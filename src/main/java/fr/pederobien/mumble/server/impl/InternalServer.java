package fr.pederobien.mumble.server.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.StringJoiner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePreEvent;
import fr.pederobien.mumble.server.impl.modifiers.LinearCircularSoundModifier;
import fr.pederobien.mumble.server.impl.request.RequestManager;
import fr.pederobien.mumble.server.interfaces.IChannelList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;
import fr.pederobien.mumble.server.persistence.MumblePersistence;
import fr.pederobien.utils.event.EventCalledEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventLogger;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.vocal.server.impl.VocalServer;
import fr.pederobien.vocal.server.interfaces.IVocalServer;

public class InternalServer implements IMumbleServer, IEventListener {
	private String name;
	private int port;
	private MumblePersistence persistence;
	private IVocalServer vocalServer;
	private TcpServer tcpServer;
	private ClientList clients;
	private IChannelList channels;
	private IServerPlayerList players;
	private RequestManager requestManagement;
	private boolean isOpened;

	/**
	 * Create a mumble server. The default communication port is 28000. In order to change the port, please turn off the server and
	 * change manually the port value in the created configuration file.
	 * 
	 * @param name The server name.
	 * @param path The folder that contains the server configuration file.
	 */
	public InternalServer(String name, String path) {
		this.name = name;

		clients = new ClientList(this);
		channels = new ChannelList(this);
		players = new ServerPlayerList(this);
		requestManagement = new RequestManager(this);

		persistence = new MumblePersistence(path);
		persistence.deserialize(this);

		registerModifiers();
		EventManager.registerListener(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void open() {
		tcpServer.connect();
		vocalServer.open();
		isOpened = true;
	}

	@Override
	public void close() {
		Runnable close = () -> {
			tcpServer.disconnect();
			vocalServer.close();
			persistence.serialize(this);
			saveLog();
			isOpened = false;
			EventManager.unregisterListener(this);
		};
		EventManager.callEvent(new ServerClosePreEvent(this), close, new ServerClosePostEvent(this));
	}

	@Override
	public boolean isOpened() {
		return isOpened;
	}

	@Override
	public IServerPlayerList getPlayers() {
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
		vocalServer = new VocalServer(name, port);
	}

	/**
	 * @return The request manager in order to perform a specific action according the remote request.
	 */
	public RequestManager getRequestManager() {
		return requestManagement;
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

	private void registerModifiers() {
		SoundManager.add(new LinearCircularSoundModifier());
	}

	private void saveLog() {
		Path logPath = Paths.get(persistence.getPath().concat(FileSystems.getDefault().getSeparator()).concat("logs"));

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
