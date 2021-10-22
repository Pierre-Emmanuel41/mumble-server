package fr.pederobien.mumble.server.impl;

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
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPreEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePreEvent;
import fr.pederobien.mumble.server.event.ServerClientCreatedEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePreEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPreEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePreEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyExistException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;
import fr.pederobien.mumble.server.exceptions.SoundModifierDoesNotExistException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventManager;

public class InternalServer {
	private MumbleServer mumbleServer;
	private TcpServerThread tcpThread;
	private UdpServerThread udpThread;
	private boolean isOpened;
	private Map<UUID, Client> clients;
	private Map<String, Channel> channels;
	private RequestManagement requestManagement;
	private Object lockChannels, lockPlayers;
	private int port;

	public InternalServer(MumbleServer mumbleServer, int port) {
		this.mumbleServer = mumbleServer;
		this.port = port;
		tcpThread = new TcpServerThread(this, port);
		udpThread = new UdpServerThread(this, port);

		clients = new HashMap<UUID, Client>();
		channels = new HashMap<String, Channel>();
		requestManagement = new RequestManagement(this);

		lockChannels = new Object();
		lockPlayers = new Object();
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
		EventManager.callEvent(new ServerClosePreEvent(mumbleServer), () -> {
			tcpThread.interrupt();
			udpThread.interrupt();
			isOpened = false;
			EventManager.callEvent(new ServerClosePostEvent(mumbleServer));
		});
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
			if (player == null)
				return null;

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
				IPlayer player = optClient.get().getPlayer();
				EventManager.callEvent(new ServerPlayerRemovePreEvent(mumbleServer, player), () -> {
					optClient.get().getPlayer().setIsOnline(false);
					optClient.get().setPlayer(null);
					EventManager.callEvent(new ServerPlayerRemovePostEvent(mumbleServer, player));
				});
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
	 * @param name              The name of the channel to add.
	 * @param soundModifierName The sound modifier name attached to the channel to add.
	 * 
	 * @return The created channel.
	 * 
	 * @throws ChannelAlreadyExistException       If there is already a channel registered for the given name.
	 * @throws SoundModifierDoesNotExistException If the sound modifier name refers to no registered modifier.
	 */
	public IChannel addChannel(String name, String soundModifierName) {
		synchronized (lockChannels) {
			IChannel existingChannel = channels.get(name);
			if (existingChannel != null)
				throw new ChannelAlreadyExistException(name);

			Optional<ISoundModifier> optSoundModifier = SoundManager.getByName(soundModifierName);
			if (!optSoundModifier.isPresent())
				throw new SoundModifierDoesNotExistException(soundModifierName);

			ServerChannelAddPreEvent event = new ServerChannelAddPreEvent(mumbleServer, name, soundModifierName);
			EventManager.callEvent(event);
			if (event.isCancelled())
				return null;

			Channel channel = new Channel(mumbleServer, name);
			channel.setSoundModifier(optSoundModifier.get());
			channels.put(channel.getName(), channel);
			EventManager.callEvent(new ServerChannelAddPostEvent(mumbleServer, channel));
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
	 * @return The port used for TCP and UDP communication.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return The mumble server associated to this internalServer.
	 */
	public IMumbleServer getMumbleServer() {
		return mumbleServer;
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
		EventManager.callEvent(new ServerClientCreatedEvent(mumbleServer, client));
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
		if (client.getPlayer() == null) {
			Player player = new Player(this, address, playerName, isAdmin);
			ServerPlayerAddPreEvent event = new ServerPlayerAddPreEvent(mumbleServer, player);
			EventManager.callEvent(event);
			if (event.isCancelled())
				return null;

			client.setPlayer(player);
			EventManager.callEvent(new ServerPlayerAddPostEvent(mumbleServer, player));
			return player;
		}
		return null;
	}

	private IChannel unsynchronizedRemove(String name) {
		if (channels.get(name) == null)
			return null;

		ServerChannelRemovePreEvent event = new ServerChannelRemovePreEvent(mumbleServer, channels.get(name));
		EventManager.callEvent(event);
		if (event.isCancelled())
			return null;

		Channel channel = channels.remove(name);
		channel.clear();
		EventManager.callEvent(new ServerChannelRemovePostEvent(mumbleServer, channel));
		return channel;
	}
}
