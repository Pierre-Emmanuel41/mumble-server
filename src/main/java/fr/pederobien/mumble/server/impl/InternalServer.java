package fr.pederobien.mumble.server.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.communication.impl.UdpServer;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPreEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePreEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePreEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyExistException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;
import fr.pederobien.mumble.server.exceptions.ServerNotOpenedException;
import fr.pederobien.mumble.server.exceptions.SoundModifierDoesNotExistException;
import fr.pederobien.mumble.server.impl.modifiers.LinearCircularSoundModifier;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.mumble.server.persistence.MumblePersistence;
import fr.pederobien.persistence.interfaces.IPersistence;
import fr.pederobien.utils.event.EventCalledEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventLogger;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class InternalServer implements IMumbleServer, IEventListener {
	private String name;
	private int port;
	private Path path;
	private IPersistence<IMumbleServer> persistence;
	private TcpServer tcpServer;
	private UdpServer udpServer;
	private ClientList clients;
	private Map<String, Channel> channels;
	private RequestManagement requestManagement;
	private Object lockChannels;
	private boolean isOpened;

	/**
	 * Create a mumble server.
	 * 
	 * @param name The server name.
	 * @param port The port for TCP and UDP communication with clients.
	 * @param path The folder that contains the server configuration file.
	 */
	public InternalServer(String name, int port, Path path) {
		this.name = name;
		this.port = port;
		this.path = path;

		persistence = new MumblePersistence(path, this);
		try {
			persistence.load(name);
		} catch (FileNotFoundException e) {
			addChannel("General", null);
		}

		tcpServer = new TcpServer(port, () -> new MessageExtractor());
		udpServer = new UdpServer(port, () -> new MessageExtractor());

		clients = new ClientList(this);
		channels = new LinkedHashMap<String, Channel>();
		requestManagement = new RequestManagement(this);

		lockChannels = new Object();

		registerModifiers();
		EventManager.registerListener(this);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Starts the tcp thread and the udp thread.
	 */
	@Override
	public void open() {
		tcpServer.connect();
		udpServer.connect();
		isOpened = true;
	}

	/**
	 * Interrupts the tcp thread and the udp thread.
	 */
	@Override
	public void close() {
		checkIsOpened();
		Runnable close = () -> {
			tcpServer.disconnect();
			udpServer.disconnect();
			persistence.save();
			saveLog();
			isOpened = false;
		};
		EventManager.callEvent(new ServerClosePreEvent(this), close, new ServerClosePostEvent(this));
		EventManager.unregisterListener(this);
	}

	/**
	 * @return True if the server is opened, false otherwise. The server is opened if and only if {@link #open()} method has been
	 *         called.
	 */
	@Override
	public boolean isOpened() {
		return isOpened;
	}

	@Override
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		checkIsOpened();
		return getClients().addPlayer(address, playerName, isAdmin);
	}

	@Override
	public void removePlayer(String playerName) {
		checkIsOpened();
		getClients().removePlayer(playerName);
	}

	@Override
	public List<IPlayer> getPlayers() {
		checkIsOpened();
		return getClients().getPlayers();
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
	@Override
	public IChannel addChannel(String name, String soundModifierName) {
		checkIsOpened();
		synchronized (lockChannels) {
			IChannel existingChannel = channels.get(name);
			if (existingChannel != null)
				throw new ChannelAlreadyExistException(name);

			Optional<ISoundModifier> optSoundModifier = SoundManager.getByName(soundModifierName);
			if (!optSoundModifier.isPresent())
				throw new SoundModifierDoesNotExistException(soundModifierName);

			ServerChannelAddPreEvent preEvent = new ServerChannelAddPreEvent(this, name, soundModifierName);
			Supplier<IChannel> add = () -> {
				Channel channel = new Channel(this, name, optSoundModifier.get());
				channels.put(channel.getName(), channel);
				return channel;
			};
			return EventManager.callEvent(preEvent, add, channel -> new ServerChannelAddPostEvent(this, channel));
		}
	}

	/**
	 * Remove the channel associated to the given name.
	 * 
	 * @param name The name of the channel to remove.
	 * 
	 * @return The removed channel if registered, null otherwise.
	 */
	@Override
	public IChannel removeChannel(String name) {
		checkIsOpened();
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
	@Override
	public void renameChannel(String oldName, String newName) {
		checkIsOpened();
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
	@Override
	public Map<String, IChannel> getChannels() {
		checkIsOpened();
		synchronized (lockChannels) {
			return new LinkedHashMap<String, IChannel>(channels);
		}
	}

	/**
	 * Removes each players from each channels registered for this server.
	 * 
	 * @return The list of removed channels.
	 */
	@Override
	public List<IChannel> clearChannels() {
		checkIsOpened();
		List<IChannel> channelsList = new ArrayList<IChannel>();
		synchronized (lockChannels) {
			List<String> names = new ArrayList<>(channels.keySet());
			int size = channels.size();
			for (int i = 0; i < size; i++)
				channelsList.add(unsynchronizedRemove(names.get(i)));
		}
		return channelsList;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("name=" + name);
		joiner.add(String.format("address = 0.0.0.0:%s", getPort()));
		return joiner.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || !(obj instanceof InternalServer))
			return false;

		InternalServer other = (InternalServer) obj;
		return getName().equals(other.getName()) && getPort() == other.getPort();
	}

	/**
	 * @return The port used for TCP and UDP communication.
	 */
	public int getPort() {
		return port;
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
	 * @return The list of clients registered for this server.
	 */
	public ClientList getClients() {
		return clients;
	}

	private IChannel unsynchronizedRemove(String name) {
		IChannel channel = channels.get(name);
		if (channel == null)
			return null;

		ServerChannelRemovePreEvent preEvent = new ServerChannelRemovePreEvent(this, channel);
		Supplier<IChannel> remove = () -> {
			IChannel c = channels.remove(name);
			c.clear();
			return c;
		};
		return EventManager.callEvent(preEvent, remove, c -> new ServerChannelRemovePostEvent(this, c));
	}

	@EventHandler
	private void onNewClientConnect(NewTcpClientEvent event) {
		if (!event.getServer().equals(tcpServer))
			return;

		getClients().createClient(event.getConnection());
	}

	@EventHandler
	private void onDataReceived(DataReceivedEvent event) {
		if (!event.getConnection().equals(udpServer.getConnection()))
			return;

		IMessage<Header> response = MumbleMessageFactory.parse(event.getBuffer());
		if (response.getHeader().getIdc() != Idc.PLAYER_SPEAK || response.getHeader().getOid() != Oid.GET)
			return;

		String playerName = (String) response.getPayload()[0];
		Optional<Client> optClient = getClients().getClient(playerName);
		if (optClient.isPresent())
			optClient.get().createUdpClient(udpServer.getConnection(), event.getAddress()).onPlayerSpeak(playerName, (byte[]) response.getPayload()[1]);
	}

	private void registerModifiers() {
		SoundManager.add(new LinearCircularSoundModifier());
	}

	private void checkIsOpened() {
		if (!isOpened)
			throw new ServerNotOpenedException();
	}

	private void saveLog() {
		Path logPath = path.resolve("logs");

		// Creates intermediate folders if they don't exist.
		if (!Files.exists(logPath))
			logPath.toFile().mkdirs();

		String name = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
		logPath = logPath.resolve(String.format("log_%s.zip", name));

		try {
			ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(logPath.toFile()));
			ZipEntry zipEntry = new ZipEntry(String.format("log_%s.txt", name));
			zipOutputStream.putNextEntry(zipEntry);

			for (EventCalledEvent event : EventLogger.instance().getEvents()) {
				String entry = String.format("[%s %s] %s\r\n", event.getTime().toLocalDate(), event.getTime().toLocalTime(), event.getEvent().toString());
				zipOutputStream.write(entry.getBytes());
			}

			zipOutputStream.closeEntry();
			zipOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
