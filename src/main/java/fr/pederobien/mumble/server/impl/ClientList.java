package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.server.event.MumbleClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClientAddPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClientAddPostEvent.Origin;
import fr.pederobien.mumble.server.event.MumbleServerClientRemovePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerPlayerRemovePostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class ClientList implements IEventListener {
	private AbstractMumbleServer server;
	private List<PlayerMumbleClient> clients;
	private Lock lock;

	/**
	 * Creates a clients list associated to a mumble server. A client is an intermediate object used to gather information about the
	 * player from the game and the information about the player in mumble.
	 * 
	 * @param server The server associated to this clients list.
	 */
	public ClientList(AbstractMumbleServer server) {
		this.server = server;
		clients = new ArrayList<PlayerMumbleClient>();
		lock = new ReentrantLock(true);

		EventManager.registerListener(this);
	}

	/**
	 * Get the client associated to the given name.
	 * 
	 * @param name The player name.
	 * 
	 * @return An optional that contains the client associated to the specified name if registered, an empty optional otherwise.
	 */
	public Optional<PlayerMumbleClient> get(String name) {
		for (PlayerMumbleClient client : clients)
			if (client.getPlayer() != null && client.getPlayer().getName().equals(name))
				return Optional.of(client);
		return Optional.empty();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onNewClient(NewTcpClientEvent event) {
		if (!event.getServer().equals(server.getTcpServer()))
			return;

		createClient(event.getConnection());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onClientDisconnected(MumbleClientDisconnectPostEvent event) {
		if (!clients.contains(event.getClient()))
			return;

		garbage(event.getClient());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onServerPlayerAdd(MumbleServerPlayerAddPostEvent event) {
		if (!event.getPlayer().getServer().equals(server))
			return;

		PlayerMumbleClient client = getOrCreateClientByGame(event.getPlayer().getGameAddress());
		client.setPlayer((Player) event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onServerPlayerRemove(MumbleServerPlayerRemovePostEvent event) {
		if (!event.getPlayer().getServer().equals(server))
			return;

		Optional<PlayerMumbleClient> optClient = get(event.getPlayer().getName());
		optClient.get().setPlayer(null);
		garbage(optClient.get());
	}

	/**
	 * Gets or creates a client associated to the socket IP address and set its mumble address.
	 * 
	 * @param socket The socket used to send data to the remote TCP client.
	 * 
	 * @return The client associated to the IP address.
	 */
	private PlayerMumbleClient createClient(ITcpConnection connection) {
		PlayerMumbleClient client = getOrCreateClientByMumble(connection);
		client.setTcpConnection(connection);
		return client;
	}

	/**
	 * Gets the client associated to the IP address and port number. If several clients are registered for this address then iterates
	 * over the list in order to find the client whose either the mumble address nor the game address correspond exactly to the IP
	 * address and port number. If none of them correspond, then it send a request to each registered client in order to check if the
	 * port associated to the game address is used on the client side.
	 * 
	 * @param address The client IP address.
	 * @param port    The client port number.
	 * 
	 * @return The client, retrieved or created, associated to the IP address and port number.
	 */
	private PlayerMumbleClient getOrCreateClientByGame(InetSocketAddress socketAddress) {
		List<PlayerMumbleClient> list = getClients(socketAddress.getAddress().getHostAddress());

		// No client registered
		if (list.isEmpty())
			return createClient(Origin.PLAYER_CONNECTED_IN_GAME, socketAddress);

		// Case 2: Clients registered
		for (PlayerMumbleClient client : list) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (client.isAssociatedTo(socketAddress.getPort()))
				return client;
		}

		Optional<PlayerMumbleClient> optClient = new GamePortAnalyzer(list).checkPortByGame(socketAddress.getPort());
		if (optClient.isPresent())
			return optClient.get();

		return createClient(Origin.PLAYER_CONNECTED_IN_GAME, socketAddress);
	}

	/**
	 * Gets the client associated to the IP address of the specified socket. If several clients are registered for this address then
	 * iterates over the list in order to find the client whose either the mumble address nor the game address correspond exactly to
	 * the IP address and port number. If none of them correspond, then it send a request to each registered client in order to check
	 * if the port associated to the game address is used on the client side.
	 * 
	 * @param socket The socket of the mumble client.
	 * 
	 * @return The client, retrieved or created, associated to the given socket.
	 */
	private PlayerMumbleClient getOrCreateClientByMumble(ITcpConnection connection) {
		List<PlayerMumbleClient> list = getClients(connection.getAddress().getAddress().getHostAddress());

		// No client registered
		if (list.isEmpty())
			return createClient(Origin.PLAYER_CONNECTED_IN_MUMBLE, connection.getAddress());

		// Case 2: Clients registered
		for (PlayerMumbleClient client : list) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (client.isAssociatedTo(connection.getAddress().getPort()))
				return client;
		}

		Optional<PlayerMumbleClient> optClient = new GamePortAnalyzer(list).checkPortByMumble(connection);
		if (optClient.isPresent())
			return optClient.get();

		return createClient(Origin.PLAYER_CONNECTED_IN_MUMBLE, connection.getAddress());
	}

	/**
	 * @return A unique identifier.
	 */
	private UUID createUUID() {
		UUID uuid;
		if (clients.isEmpty())
			uuid = UUID.randomUUID();
		else {
			boolean uuidAlreadyExists = false;
			do {
				uuid = UUID.randomUUID();
				for (PlayerMumbleClient client : clients) {
					if (client.getUUID().equals(uuid)) {
						uuidAlreadyExists = true;
						break;
					}
				}
				if (uuidAlreadyExists)
					break;
			} while (uuidAlreadyExists);
		}
		return uuid;
	}

	private PlayerMumbleClient createClient(Origin origin, InetSocketAddress address) {
		PlayerMumbleClient client = new PlayerMumbleClient(server, createUUID());
		clients.add(client);
		EventManager.callEvent(new MumbleServerClientAddPostEvent(server, client, origin, address));
		return client;
	}

	/**
	 * Thread safe operation to get the list of clients associated to the given name.
	 * 
	 * @param hostAddress The IP address without the port number
	 * 
	 * @return The list of registered clients.
	 */
	private List<PlayerMumbleClient> getClients(String hostAddress) {
		List<PlayerMumbleClient> list = new ArrayList<PlayerMumbleClient>();
		lock.lock();
		try {
			for (PlayerMumbleClient client : clients)
				if (client.isAssociatedTo(hostAddress))
					list.add(client);
		} finally {
			lock.unlock();
		}
		return list;
	}

	/**
	 * Thread safe operation to remove all clients from this list.
	 */
	public void clear() {
		lock.lock();
		try {
			clients.clear();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation to remove the given client from the registered clients list.
	 * 
	 * @param client The client to remove.
	 */
	private void removeClient(PlayerMumbleClient client) {
		lock.lock();
		boolean removed = false;
		try {
			removed = clients.remove(client);
		} finally {
			lock.unlock();
		}

		if (removed)
			EventManager.callEvent(new MumbleServerClientRemovePostEvent(server, client));
	}

	/**
	 * Removes the given client if and only if the player and the mumble client are disconnected.
	 * 
	 * @param client The client to remove.
	 */
	private void garbage(PlayerMumbleClient client) {
		if (client.getPlayer() == null && (client.getTcpConnection() == null || client.getTcpConnection().isDisposed()))
			removeClient(client);
	}
}
