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
import java.util.stream.Collectors;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyExistException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;
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
	private Map<String, IChannel> channels;
	private Observable<IObsServer> observers;
	private RequestManagement requestManagement;
	private Object lockChannels, lockPlayers;
	private int udpPort;

	public InternalServer(InetAddress address, int tcpPort, int udpPort) {
		this.udpPort = udpPort;
		tcpThread = new TcpServerThread(this, address, tcpPort);
		udpThread = new UdpServerThread(this, address, udpPort);

		clients = new HashMap<UUID, Client>();
		channels = new HashMap<String, IChannel>();
		observers = new Observable<IObsServer>();
		requestManagement = new RequestManagement(this);

		lockChannels = new Object();
		lockPlayers = new Object();
	}

	@Override
	public void addObserver(IObsServer obs) {
		observers.addObserver(obs);
	}

	@Override
	public void removeObserver(IObsServer obs) {
		observers.removeObserver(obs);
	}

	/**
	 * Starts the tcp thread and the udp thread.
	 */
	public void open() {
		tcpThread.start();
		udpThread.start();
		isOpened = true;
	}

	/**
	 * Interrupts the tcp thread and the udp thread.
	 */
	public void close() {
		observers.notifyObservers(obs -> obs.onServerClosing());
		tcpThread.interrupt();
		udpThread.interrupt();
		isOpened = false;
	}

	/**
	 * @return True if the server is opened, false otherwise. The server is opened if and only if {@link #open()} method has been
	 *         called.
	 */
	public boolean isOpened() {
		return isOpened;
	}

	/**
	 * First check if a client is registered for the given address, if any then returns it, else creates a new client object.
	 * 
	 * @param address The address used to retrieve or create a client.
	 * 
	 * @return The client associated to the given address.
	 */
	public Client getOrCreateClient(InetSocketAddress address) {
		synchronized (lockPlayers) {
			Optional<Client> optClient = clients.values().stream().filter(client -> client.getAddress().getAddress().equals(address.getAddress())).findFirst();
			if (optClient.isPresent())
				return optClient.get();
			else
				return createClient(address);
		}
	}

	/**
	 * Creates based on the given properties.
	 * 
	 * @param address    The address of the player in game.
	 * @param playerName The name of the player in game.
	 * @param isAdmin    The admin status of the player in game.
	 * 
	 * @return The created player.
	 */
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		synchronized (lockPlayers) {
			Player player = getOrCreatePlayer(address, playerName, isAdmin);
			player.setIsOnline(true);
			return player;
		}
	}

	/**
	 * Removes the player associated to the given playerName.
	 * 
	 * @param playerName The name of the player to remove.
	 */
	public void removePlayer(String playerName) {
		synchronized (lockPlayers) {
			Optional<Client> optClient = clients.values().stream().filter(c -> c.getPlayer() != null && c.getPlayer().getName().equals(playerName)).findFirst();
			if (optClient.isPresent() && optClient.get().getPlayer() != null) {
				optClient.get().getPlayer().setIsOnline(false);
				optClient.get().setPlayer(null);
			}
		}
	}

	/**
	 * @return The list of player currently registered for this server.
	 */
	public List<IPlayer> getPlayers() {
		return Collections.unmodifiableList(clients.values().stream().map(client -> client.getPlayer()).filter(player -> player != null).collect(Collectors.toList()));
	}

	/**
	 * Try to find the player associated to the specified playerName.
	 * 
	 * @param playerName The name of the player to return.
	 * 
	 * @return An optional that contains a player if found, an empty optional otherwise.
	 */
	public Optional<Player> getPlayer(String playerName) {
		synchronized (lockPlayers) {
			return clients.values().stream().map(client -> client.getPlayer()).filter(player -> player != null && player.getName().equals(playerName)).findFirst();
		}
	}

	/**
	 * Creates a channel associated to the given name.
	 * 
	 * @param name The name of the channel to add.
	 * 
	 * @return The created channel.
	 * 
	 * @throws ChannelAlreadyExistException If there is already a channel registered for the given name.
	 */
	public IChannel addChannel(String name) {
		synchronized (lockChannels) {
			IChannel existingChannel = channels.get(name);
			if (existingChannel != null)
				throw new ChannelAlreadyExistException(name);

			Channel channel = new Channel(name);
			channels.put(channel.getName(), channel);
			observers.notifyObservers(obs -> obs.onChannelAdded(channel));
			addObserver(channel);
			return channel;
		}
	}

	/**
	 * Remove the channel associated to the given name.
	 * 
	 * @param name The name of the channel to remove.
	 * 
	 * @return The removed channel if registered, null otherwise.
	 */
	public IChannel removeChannel(String name) {
		synchronized (lockChannels) {
			return unsynchronizedRemove(name);
		}
	}

	/**
	 * Set the name of the channel associated to the specified oldName.
	 * 
	 * @param oldName The old channel name.
	 * @param newName The new channel name.
	 * 
	 * @throws ChannelNotRegisteredException If there is no channels associated to the oldName.
	 * @throws ChannelAlreadyExistException  If there is already a channel registered for the given newName.
	 */
	public void renameChannel(String oldName, String newName) {
		Map<String, IChannel> channels = getChannels();
		Channel channel = (Channel) channels.get(oldName);
		if (channel == null)
			throw new ChannelNotRegisteredException(oldName);

		if (channels.get(newName) != null)
			throw new ChannelAlreadyExistException(newName);

		channel.setName(newName);
		synchronized (lockChannels) {
			channels.remove(oldName);
			channels.put(newName, channel);
		}
	}

	/**
	 * Creates a copy of the current channels map.
	 * 
	 * @return The created copy.
	 */
	public Map<String, IChannel> getChannels() {
		synchronized (lockChannels) {
			return new HashMap<>(channels);
		}
	}

	/**
	 * Removes each players from each channels registered for this server.
	 * 
	 * @return The list of removed channels.
	 */
	public List<IChannel> clearChannels() {
		List<IChannel> channelsList = new ArrayList<IChannel>();
		synchronized (lockChannels) {
			List<String> names = new ArrayList<>(channels.keySet());
			int size = channels.size();
			for (int i = 0; i < size; i++)
				channelsList.add(unsynchronizedRemove(names.get(i)));
		}
		return channelsList;
	}

	/**
	 * Try to answer to the specified event.
	 * 
	 * @param event The event that contains the client which received a request from the network.
	 * 
	 * @return A message that contains the answer.
	 */
	public IMessage<Header> answer(RequestEvent event) {
		return requestManagement.answer(event);
	}

	/**
	 * @return The udp port used for the vocal communication.
	 */
	public int getUdpPort() {
		return udpPort;
	}

	/**
	 * Notify each client the mute status of the player associated to the given name has changed.
	 * 
	 * @param playerName The name of the player whose mute status has changed.
	 * @param isMute     True if the player is now muted, false otherwise.
	 */
	public void onPlayerMuteChanged(String playerName, boolean isMute) {
		clients.values().stream().forEach(client -> client.onPlayerMuteChanged(playerName, isMute));
	}

	/**
	 * Notify each client the deafen status of the player associated to the given name has changed.
	 * 
	 * @param playerName The name of the player whose deafen status has changed.
	 * @param isMute     True if the player is now deafen, false otherwise.
	 */
	public void onPlayerDeafenChanged(String playerName, boolean isDeafen) {
		clients.values().forEach(client -> client.onPlayerDeafenChanged(playerName, isDeafen));
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
			client.setPlayer(new Player(this, address, playerName, isAdmin));
		return client.getPlayer();
	}

	private IChannel unsynchronizedRemove(String name) {
		Channel channel = (Channel) channels.remove(name);
		if (channel != null) {
			channel.clear();
			observers.notifyObservers(obs -> obs.onChannelRemoved(channel));
			removeObserver(channel);
		}
		return channel;
	}
}
