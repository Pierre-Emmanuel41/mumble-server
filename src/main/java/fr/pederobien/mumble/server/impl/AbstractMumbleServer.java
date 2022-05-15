package fr.pederobien.mumble.server.impl;

import java.util.concurrent.atomic.AtomicInteger;

import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.mumble.common.impl.MumbleMessageExtractor;
import fr.pederobien.mumble.server.impl.modifiers.LinearCircularSoundModifier;
import fr.pederobien.mumble.server.impl.request.ServerRequestManager;
import fr.pederobien.mumble.server.interfaces.IChannelList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;
import fr.pederobien.mumble.server.interfaces.IServerRequestManager;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.vocal.server.impl.VocalServer;
import fr.pederobien.vocal.server.interfaces.IVocalServer;

public abstract class AbstractMumbleServer implements IMumbleServer {
	private String name;
	private AtomicInteger mumblePort;
	private IVocalServer vocalServer;
	private TcpServer tcpServer;
	private IChannelList channels;
	private IServerPlayerList players;
	private IServerRequestManager serverRequestManager;
	private ClientList clients;

	/**
	 * Creates a mumble server with a specific name.
	 * 
	 * @param name The server name.
	 */
	protected AbstractMumbleServer(String name) {
		this.name = name;

		mumblePort = new AtomicInteger(-1);
		channels = new ChannelList(this);
		players = new ServerPlayerList(this);
		serverRequestManager = new ServerRequestManager(this);
		clients = new ClientList(this);

		registerModifiers();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void open() {
		tcpServer.connect();
		vocalServer.open();
	}

	@Override
	public void close() {
		tcpServer.disconnect();
		vocalServer.close();
		clients.clear();
		EventManager.unregisterListener(clients);
	}

	@Override
	public IServerPlayerList getPlayers() {
		return players;
	}

	@Override
	public IChannelList getChannels() {
		return channels;
	}

	/**
	 * @return The manager responsible to update the server configuration according to the reception of configuration requests.
	 */
	public IServerRequestManager getRequestManager() {
		return serverRequestManager;
	}

	/**
	 * Set the port number on which the server receives configuration requests and on which players talk together. For internal use
	 * only.
	 * 
	 * @param mumblePort The port on which the server receives configuration requests and on which players talk together.
	 */
	public void setMumblePort(int mumblePort) {
		if (!this.mumblePort.compareAndSet(-1, mumblePort))
			throw new IllegalStateException("The port number has already been set");

		tcpServer = new TcpServer(name, mumblePort, () -> new MumbleMessageExtractor(), true);
		vocalServer = new VocalServer(name, mumblePort);
	}

	/**
	 * For internal use only.
	 * 
	 * @return The port on which the server receives configuration requests and on which players talk together.
	 */
	public int getMumblePort() {
		return mumblePort.get();
	}

	/**
	 * @return The TCP server on which configuration request are sent.
	 */
	protected TcpServer getTcpServer() {
		return tcpServer;
	}

	private void registerModifiers() {
		SoundManager.add(new LinearCircularSoundModifier());
	}
}
