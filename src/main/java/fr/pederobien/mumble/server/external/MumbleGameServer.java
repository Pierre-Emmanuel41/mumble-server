package fr.pederobien.mumble.server.external;

import java.net.Socket;
import java.nio.file.Path;

import fr.pederobien.communication.impl.TcpServerConnection;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.server.impl.MumbleServer;

public class MumbleGameServer extends MumbleServer {
	private Thread gameServerThread;
	private MumbleGameServerClient gameServerClient;

	/**
	 * Creates an external mumble server. This kind of server is used when it should be running outside from the game server.
	 * 
	 * @param name           The server name.
	 * @param address        The server IP address.
	 * @param gameServerPort The port number for the TCP communication between the game server and this server.
	 * @param tcpPort        The port number for the TCP communication between this server and the future Mumble client.
	 * @param udpPort        The port number for the UDP communication (voice) between this server and the future Mumble client.
	 * @param path           The folder that contains the server configuration file.
	 */
	public MumbleGameServer(String name, int gameServerPort, int tcpPort, int udpPort, Path path) {
		super(name, tcpPort, udpPort, path);
		gameServerThread = new MumbleGameServerThread(this, gameServerPort);
	}

	/**
	 * Open this server.
	 */
	public void open() {
		super.open();
		gameServerThread.start();
	}

	/**
	 * Close this server.
	 */
	public void close() {
		super.close();
		gameServerThread.interrupt();
	}

	/**
	 * Create a client associated to the game server.
	 * 
	 * @param socket The socket used to send/receive data from the game server.
	 */
	protected void createGameServerClient(Socket socket) {
		if (gameServerClient == null)
			gameServerClient = new MumbleGameServerClient(getInternalServer(), new TcpServerConnection(socket, new MessageExtractor()));
	}

	/**
	 * @return The client associated to the game server. It is not null if and only if the game server is running and connected.
	 */
	public MumbleGameServerClient getGameServerClient() {
		return gameServerClient;
	}
}
