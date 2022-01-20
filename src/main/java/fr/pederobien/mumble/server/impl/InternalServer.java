package fr.pederobien.mumble.server.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.StringJoiner;
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
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePreEvent;
import fr.pederobien.mumble.server.impl.modifiers.LinearCircularSoundModifier;
import fr.pederobien.mumble.server.interfaces.IChannelList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayerList;
import fr.pederobien.mumble.server.persistence.MumblePersistence;
import fr.pederobien.utils.event.EventCalledEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventLogger;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class InternalServer implements IMumbleServer, IEventListener {
	private String name;
	private int port;
	private Path path;
	private MumblePersistence persistence;
	private TcpServer tcpServer;
	private UdpServer udpServer;
	private ClientList clients;
	private IChannelList channels;
	private IPlayerList players;
	private RequestManagement requestManagement;
	private boolean isOpened;

	/**
	 * Create a mumble server. The default communication port is 28000. In order to change the port, please turn off the server and
	 * change manually the port value in the created configuration file.
	 * 
	 * @param name The server name.
	 * @param path The folder that contains the server configuration file.
	 */
	public InternalServer(String name, Path path) {
		this.name = name;
		this.path = path;

		clients = new ClientList(this);
		channels = new ChannelList(this);
		players = new PlayerList(this);
		requestManagement = new RequestManagement(this);

		persistence = new MumblePersistence();
		persistence.deserialize(this, path);

		registerModifiers();
		EventManager.registerListener(this);
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Starts the TCP thread and the UDP thread.
	 */
	@Override
	public void open() {
		tcpServer.connect();
		udpServer.connect();
		isOpened = true;
	}

	/**
	 * Interrupts the TCP thread and the UDP thread.
	 */
	@Override
	public void close() {
		Runnable close = () -> {
			tcpServer.disconnect();
			udpServer.disconnect();
			persistence.serialize(this, path);
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
	public IPlayerList getPlayers() {
		return players;
	}

	@Override
	public IChannelList getChannels() {
		return channels;
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
	 * Set the port used for TCP and UDP communication.
	 * 
	 * @param port The port used for communication.
	 */
	public void setPort(int port) {
		this.port = port;
		tcpServer = new TcpServer(name, port, () -> new MessageExtractor());
		udpServer = new UdpServer(name, port, () -> new MessageExtractor());
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
