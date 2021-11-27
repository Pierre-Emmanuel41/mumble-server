package fr.pederobien.mumble.server.external;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.mumble.common.impl.MessageExtractor;
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
	 * Creates an external mumble server. This kind of server is used when it should be running outside from the game server.
	 * 
	 * @param name           The server name.
	 * @param address        The server IP address.
	 * @param gameServerPort The port number for the TCP communication between the game server and this server.
	 * @param port           The port number for the TCP and UDP communication between this server and Mumble clients.
	 * @param path           The folder that contains the server configuration file.
	 */
	public MumbleGameServer(String name, int gameServerPort, int port, Path path) {
		server = new InternalServer(name, port, path);
		tcpServer = new TcpServer(gameServerPort, () -> new MessageExtractor());
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
		server.close();
		tcpServer.disconnect();
		client = null;
	}

	@Override
	public boolean isOpened() {
		return server.isOpened();
	}

	@Override
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		return server.addPlayer(address, playerName, isAdmin);
	}

	@Override
	public void removePlayer(String playerName) {
		server.removePlayer(playerName);
	}

	@Override
	public List<IPlayer> getPlayers() {
		return server.getPlayers();
	}

	@Override
	public IChannel addChannel(String name, String soundModifierName) {
		return server.addChannel(name, soundModifierName);
	}

	@Override
	public IChannel removeChannel(String name) {
		return server.removeChannel(name);
	}

	@Override
	public void renameChannel(String oldName, String newName) {
		server.renameChannel(oldName, newName);
	}

	@Override
	public Map<String, IChannel> getChannels() {
		return server.getChannels();
	}

	@Override
	public List<IChannel> clearChannels() {
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
}
