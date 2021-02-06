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
	private TcpServerThread tcpThread;
	private UdpServerThread udpThread;
	private boolean isOpened;
	private Map<UUID, Client> clients;
	private List<IChannel> channels;
	private Observable<IObsServer> observers;
	private RequestManagement requestManagement;
	private Object lockChannels, lockPlayers;
	private int udpPort;

	public InternalServer(InetAddress address, int tcpPort, int udpPort) {
		this.udpPort = udpPort;
		tcpThread = new TcpServerThread(this, address, tcpPort);
		udpThread = new UdpServerThread(this, address, udpPort);

		clients = new HashMap<UUID, Client>();
		channels = new ArrayList<IChannel>();
		observers = new Observable<IObsServer>();
		requestManagement = new RequestManagement(this);

		lockChannels = new Object();
		lockPlayers = new Object();

		addChannel("General");
		addChannel("Channel 0");
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
		tcpThread.start();
		udpThread.start();
		isOpened = true;
	}

	public void close() {
		notifyObservers(obs -> obs.onServerClosing());
		tcpThread.interrupt();
		udpThread.interrupt();
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
			return createClient(address);
	}

	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		synchronized (lockPlayers) {
			Player player = getOrCreatePlayer(address, playerName, isAdmin);
			player.setIsOnline(true);
			return player;
		}
	}

	public void removePlayer(String playerName) {
		synchronized (lockPlayers) {
			Optional<Client> optClient = clients.values().stream().filter(c -> c.getPlayer() != null && c.getPlayer().getName().equals(playerName)).findFirst();
			if (optClient.isPresent())
				optClient.get().getPlayer().setIsOnline(false);
		}
	}

	public List<IPlayer> getPlayers() {
		return Collections.unmodifiableList(clients.values().stream().map(client -> client.getPlayer()).filter(player -> player != null).collect(Collectors.toList()));
	}

	public IChannel addChannel(String name) {
		synchronized (lockChannels) {
			IChannel existingChannel = getChannel(name);
			if (existingChannel != null)
				throw new ChannelAlreadyExistException(name);

			Channel channel = new Channel(name);
			channels.add(channel);
			notifyObservers(obs -> obs.onChannelAdded(channel));
			addObserver(channel);
			return channel;
		}
	}

	public IChannel removeChannel(String name) {
		synchronized (lockChannels) {
			Channel channel = (Channel) getChannel(name);
			if (channel != null) {
				channels.remove(channel);
				notifyObservers(obs -> obs.onChannelRemoved(channel));
				removeObserver(channel);
			}
			return channel;
		}
	}

	public List<IChannel> getChannels() {
		synchronized (lockChannels) {
			return Collections.unmodifiableList(channels);
		}
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

	public int getUdpPort() {
		return udpPort;
	}

	private void notifyObservers(Consumer<IObsServer> consumer) {
		observers.notifyObservers(consumer);
	}

	private Client createClient(InetSocketAddress address) {
		UUID uuid = createUUID();
		Client client = new Client(this, uuid, address);
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

	private Player getOrCreatePlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		Client client = getOrCreateClient(address);
		if (client.getPlayer() == null)
			client.setPlayer(new Player(address, playerName, isAdmin));
		return client.getPlayer();
	}
}
