package fr.pederobien.mumble.server.external;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.server.exceptions.ServerNotOpenedException;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class MumbleGameServer implements IMumbleServer, IEventListener {
	private InternalServer server;
	private TcpServer tcpServer;
	private MumbleGameServerClient client;

	/**
	 * Creates an external mumble server. This kind of server is used when it should be running outside from the game server. The
	 * default communication port is 28000. In order to change the port, please turn off the server and change manually the port value
	 * in the created configuration file.
	 * 
	 * @param name           The server name.
	 * @param address        The server IP address.
	 * @param gameServerPort The port number for the TCP communication between the game server and this server.
	 * @param path           The folder that contains the server configuration file.
	 */
	public MumbleGameServer(String name, int gameServerPort, Path path) {
		server = new InternalServer(name, path);
		tcpServer = new TcpServer(name, gameServerPort, () -> new MessageExtractor());
		EventManager.registerListener(this);
	}

	@Override
	public String getName() {
		return server.getName();
	}

	@Override
	public void open() {
		server.open();
		tcpServer.connect();
	}

	@Override
	public void close() {
		checkIsOpened();
		server.close();
		tcpServer.disconnect();
		client = null;
	}

	@Override
	public boolean isOpened() {
		checkIsOpened();
		return server.isOpened();
	}

	@Override
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		checkIsOpened();
		return server.addPlayer(address, playerName, isAdmin);
	}

	@Override
	public void removePlayer(String playerName) {
		checkIsOpened();
		server.removePlayer(playerName);
	}

	@Override
	public List<IPlayer> getPlayers() {
		checkIsOpened();
		return server.getPlayers();
	}

	@Override
	public IChannel addChannel(String name, String soundModifierName) {
		checkIsOpened();
		return server.addChannel(name, soundModifierName);
	}

	@Override
	public IChannel removeChannel(String name) {
		checkIsOpened();
		return server.removeChannel(name);
	}

	@Override
	public void renameChannel(String oldName, String newName) {
		checkIsOpened();
		server.renameChannel(oldName, newName);
	}

	@Override
	public Map<String, IChannel> getChannels() {
		checkIsOpened();
		return server.getChannels();
	}

	@Override
	public List<IChannel> clearChannels() {
		checkIsOpened();
		return server.clearChannels();
	}

	@EventHandler
	private void onNewClientConnect(NewTcpClientEvent event) {
		if (!event.getServer().equals(tcpServer))
			return;

		// Only one game server can be connected to this mumble server.
		if (client != null)
			event.getConnection().dispose();
		else
			client = new MumbleGameServerClient(server, event.getConnection());
	}

	/**
	 * @return The client associated to the game server. It is not null if the server is running.
	 */
	public MumbleGameServerClient getGameServerClient() {
		return client;
	}

	private void checkIsOpened() {
		if (!server.isOpened())
			throw new ServerNotOpenedException();
	}
}
