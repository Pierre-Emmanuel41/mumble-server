package fr.pederobien.mumble.server.impl;

import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import fr.pederobien.communication.event.ConnectionDisposedEvent;
import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.MumbleMessageExtractor;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePreEvent;
import fr.pederobien.mumble.server.event.ServerOpenPostEvent;
import fr.pederobien.mumble.server.event.ServerOpenPreEvent;
import fr.pederobien.mumble.server.persistence.StandaloneMumbleServerPersistence;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class StandaloneMumbleServer extends AbstractMumbleServer implements IEventListener {
	private static final String GAME_CLIENT = "GameClient";
	private AtomicInteger externalGameServerPort;
	private StandaloneMumbleServerPersistence persistence;
	private TcpServer tcpServer;
	private StandaloneMumbleClient client;
	private ITcpConnection connection;
	private AtomicBoolean isOpened, canConnect;

	/**
	 * Creates an standalone mumble server. This kind of server is used when it should be running outside from the game server. The
	 * default port number for the server configuration requests and for the audio communication between players is 28000. The default
	 * port number for the communication with the external game server is 29000. In order to change the port, please turn off the
	 * server and change manually the port values in the created configuration file.
	 * 
	 * @param name The server name.
	 * @param path The folder that contains the server configuration file.
	 */
	public StandaloneMumbleServer(String name, String path) {
		super(name);

		externalGameServerPort = new AtomicInteger(-1);
		isOpened = new AtomicBoolean(false);
		canConnect = new AtomicBoolean(true);

		persistence = new StandaloneMumbleServerPersistence(path);
		persistence.deserialize(this);

		EventManager.registerListener(this);
	}

	@Override
	public void open() {
		if (!isOpened.compareAndSet(false, true))
			return;

		Runnable update = () -> {
			super.open();
			tcpServer.connect();
		};
		EventManager.callEvent(new ServerOpenPreEvent(this), update, new ServerOpenPostEvent(this));
	}

	@Override
	public void close() {
		if (!isOpened.compareAndSet(true, false))
			return;

		Runnable update = () -> {
			super.close();
			tcpServer.disconnect();
			persistence.serialize(this);
			client = null;
		};
		EventManager.callEvent(new ServerClosePreEvent(this), update, new ServerClosePostEvent(this));
	}

	@Override
	public boolean isOpened() {
		return isOpened.get();
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("name=" + getName());
		joiner.add(String.format("mumble port = %s", getMumblePort()));
		joiner.add(String.format("game server port = %s", getExternalGameServerPort()));
		return joiner.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof StandaloneMumbleServer))
			return false;

		StandaloneMumbleServer other = (StandaloneMumbleServer) obj;
		return getName().equals(other.getName()) && getMumblePort() == other.getMumblePort() && getExternalGameServerPort() == other.getExternalGameServerPort();
	}

	/**
	 * Set the port number on which there is the communication with the external game server. For internal use only.
	 * 
	 * @param externalGameServerPort The port number on which there is the communication with the external game server.
	 */
	public void setExternalGameServerPort(int externalGameServerPort) {
		if (!this.externalGameServerPort.compareAndSet(-1, externalGameServerPort))
			throw new IllegalStateException("The port number has already been set");

		tcpServer = new TcpServer(String.format("%s%s", getName(), GAME_CLIENT), externalGameServerPort, () -> new MumbleMessageExtractor(), true);
	}

	/**
	 * @return The port number on which there is the communication with the external game server.
	 */
	public int getExternalGameServerPort() {
		return externalGameServerPort.get();
	}

	@EventHandler
	private void onNewClientConnect(NewTcpClientEvent event) {
		if (!event.getServer().equals(tcpServer))
			return;

		// Only one game server can be connected to this mumble server.
		if (!canConnect.compareAndSet(true, false))
			event.getConnection().dispose();
		else
			client = new StandaloneMumbleClient(this, connection = event.getConnection());
	}

	@EventHandler
	private void onConnectionDispose(ConnectionDisposedEvent event) {
		if (!event.getConnection().equals(connection))
			return;

		canConnect.set(true);
	}

	/**
	 * @return The client associated to the game server. It is not null if the server is running.
	 */
	public StandaloneMumbleClient getGameServerClient() {
		return client;
	}
}
