package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.pederobien.communication.impl.BlockingQueueTask;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyExistException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;
import fr.pederobien.utils.IObservable;
import fr.pederobien.utils.Observable;

public class InternalServer implements IObservable<IObsServer> {
	private TCPServerThread tcpThread;
	private boolean isOpened;
	private Map<UUID, Client> clients;
	private List<IChannel> channels;
	private Observable<IObsServer> observers;
	private RequestManagement requestManagement;
	private BlockingQueueTask<Runnable> actions;

	public InternalServer(InetAddress address, int tcpPort, int udpPort) {
		tcpThread = new TCPServerThread(this, address, tcpPort);
		clients = new HashMap<UUID, Client>();
		channels = new ArrayList<IChannel>();
		observers = new Observable<IObsServer>();
		requestManagement = new RequestManagement(this);

		actions = new BlockingQueueTask<>("ScheduledActions", runnable -> runLater(runnable));

		addChannel("General");
	}

	@Override
	public void addObserver(IObsServer obs) {
		observers.addObserver(obs);
	}

	@Override
	public void removeObserver(IObsServer obs) {
		observers.removeObserver(obs);
	}

	public void open() {
		actions.start();
		tcpThread.start();
		isOpened = true;
	}

	public void close() {
		notifyObservers(obs -> obs.onServerClosing());
		tcpThread.interrupt();
		actions.dispose();
		isOpened = false;
	}

	public boolean isOpened() {
		return isOpened;
	}

	public Client getOrCreateClient(InetSocketAddress address) {
		Optional<Client> optClient = clients.values().stream().filter(client -> client.getAddress().getAddress().equals(address.getAddress())).findFirst();
		if (optClient.isPresent())
			return optClient.get();
		else
			return createClient();
	}

	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		Player player = new Player(address, playerName, isAdmin);
		player.setClient(getOrCreateClient(player.getIp()));
		return player;
	}

	public void removePlayer(String playerName) {
		Optional<Client> optClient = clients.values().stream().filter(c -> c.getPlayer() != null && c.getPlayer().getName().equals(playerName)).findFirst();
		if (optClient.isPresent())
			optClient.get().setPlayer(null);
	}

	public List<IPlayer> getPlayers() {
		return Collections.unmodifiableList(clients.values().stream().map(client -> client.getPlayer()).filter(player -> player != null).collect(Collectors.toList()));
	}

	public IChannel addChannel(String name) {
		IChannel existingChannel = getChannel(name);
		if (existingChannel != null)
			throw new ChannelAlreadyExistException(name);

		IChannel channel = new Channel(name);
		channels.add(channel);
		notifyObservers(obs -> obs.onChannelAdded(channel));
		return channel;
	}

	public IChannel removeChannel(String name) {
		IChannel channel = getChannel(name);
		if (channel != null) {
			channels.remove(channel);
			notifyObservers(obs -> obs.onChannelRemoved(channel));
		}
		return channel;
	}

	public List<IChannel> getChannels() {
		return Collections.unmodifiableList(channels);
	}

	public IChannel getChannel(String channelName) {
		Optional<IChannel> optChannel = channels.stream().filter(c -> c.getName().equals(channelName)).findFirst();
		return optChannel.isPresent() ? optChannel.get() : null;
	}

	public void clearChannels() {
		channels.clear();
	}

	public Map<UUID, Client> getClients() {
		return clients;
	}

	public IMessage<Header> answer(RequestEvent event) {
		return requestManagement.answer(event);
	}

	public void ScheduleAction(Runnable runnable) {
		actions.add(runnable);
	}

	private void notifyObservers(Consumer<IObsServer> consumer) {
		observers.notifyObservers(consumer);
	}

	private Client createClient() {
		UUID uuid = createUUID();
		Client client = new Client(this, clients);
		client.setUUID(uuid);
		clients.put(uuid, client);
		return client;
	}

	private UUID createUUID() {
		UUID uuid;
		if (clients.isEmpty())
			uuid = UUID.randomUUID();
		else {
			boolean uuidAlreadyExists;
			do {
				final UUID uuidToTest = uuid = UUID.randomUUID();
				uuidAlreadyExists = clients.keySet().stream().filter(id -> id.equals(uuidToTest)).findFirst().isPresent();
			} while (uuidAlreadyExists);
		}
		return uuid;
	}

	private void runLater(Runnable runnable) {
		try {
			Thread.sleep(10);
			runnable.run();
		} catch (InterruptedException e) {

		}
	}
}
