package fr.pederobien.mumble.server.impl;

import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.mumble.common.impl.MumbleMessageExtractor;
import fr.pederobien.mumble.server.impl.modifiers.LinearCircularSoundModifier;
import fr.pederobien.mumble.server.impl.request.ServerRequestManager;
import fr.pederobien.mumble.server.interfaces.IChannelList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;
import fr.pederobien.mumble.server.interfaces.IServerRequestManager;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.vocal.server.impl.SpeakBehavior;
import fr.pederobien.vocal.server.impl.VocalServer;
import fr.pederobien.vocal.server.interfaces.IVocalServer;

public abstract class AbstractMumbleServer implements IMumbleServer {
	private static final String CONFIGURATION = "Configuration";
	private static final String VOCAL = "Vocal";

	private String name;
	private int configurationPort, vocalPort;
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof AbstractMumbleServer))
			return false;

		AbstractMumbleServer other = (AbstractMumbleServer) obj;
		return getName().equals(other.getName()) && getConfigurationPort() == other.getConfigurationPort() && getVocalPort() == other.getVocalPort();
	}

	/**
	 * @return The manager responsible to update the server configuration according to the reception of configuration requests.
	 */
	public IServerRequestManager getRequestManager() {
		return serverRequestManager;
	}

	/**
	 * Set the port number on which the server receives configuration requests. For internal use only.
	 * 
	 * @param configurationPort The port on which the server receives configuration requests.
	 */
	public void setConfigurationPort(int configurationPort) {
		if (tcpServer != null && tcpServer.isConnected())
			tcpServer.disconnect();

		this.configurationPort = configurationPort;
		tcpServer = new TcpServer(String.format("%s_%s", name, CONFIGURATION), configurationPort, () -> new MumbleMessageExtractor(), true);
	}

	/**
	 * Set the port number on which the underlying vocal server receives configuration requests and on which players talk together.
	 * For internal use only.
	 * 
	 * @param vocalPort The port on which the underlying vocal server receives configuration requests and on which players talk
	 *                  together.
	 * 
	 * @throws IllegalStateException if the vocal port equals to the configuration port.
	 */
	public void setVocalPort(int vocalPort) {
		if (vocalPort == getConfigurationPort())
			throw new IllegalStateException("The vocal port number must not be equals to the configuration port number");

		if (vocalServer != null && vocalServer.isOpened())
			vocalServer.close();

		this.vocalPort = vocalPort;
		vocalServer = new VocalServer(String.format("%s_%s", name, VOCAL), vocalPort, SpeakBehavior.TO_NO_ONE);
	}

	/**
	 * For internal use only.
	 * 
	 * @return The port on which the server receives configuration requests.
	 */
	public int getConfigurationPort() {
		return configurationPort;
	}

	/**
	 * For internal use only.
	 * 
	 * @return The port on which the underlying vocal server receives configuration requests and on which players talk together.
	 */
	public int getVocalPort() {
		return vocalPort;
	}

	/**
	 * @return The list of clients associated to this server.
	 */
	public ClientList getClients() {
		return clients;
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
