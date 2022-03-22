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
import fr.pederobien.mumble.server.event.ClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.ServerClientAddPostEvent;
import fr.pederobien.mumble.server.event.ServerClientAddPostEvent.Origin;
import fr.pederobien.mumble.server.event.ServerClientRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class ClientList implements IEventListener {
	private InternalServer server;
	private List<MumblePlayerClient> clients;
	private Lock lock;

	public ClientList(InternalServer internalServer) {
		this.server = internalServer;
		clients = new ArrayList<MumblePlayerClient>();
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
	public Optional<MumblePlayerClient> get(String name) {
		for (MumblePlayerClient client : clients)
			if (client.getPlayer() != null && client.getPlayer().isOnline() && client.getPlayer().getName().equals(name))
				return Optional.of(client);
		return Optional.empty();
	}

	@EventHandler
	private void onNewClient(NewTcpClientEvent event) {
		if (!event.getServer().equals(server.getTcpServer()))
			return;

		createClient(event.getConnection());
	}

	@EventHandler
	private void onClientDisconnected(ClientDisconnectPostEvent event) {
		garbage(event.getClient());
	}

	@EventHandler
	private void onServerPlayerAdd(ServerPlayerAddPostEvent event) {
		MumblePlayerClient client = getOrCreateClientByGame(event.getPlayer().getGameAddress());
		client.setPlayer((Player) event.getPlayer());
	}

	@EventHandler
	private void onServerPlayerRemove(ServerPlayerRemovePostEvent event) {
		Optional<MumblePlayerClient> optClient = get(event.getPlayer().getName());
		optClient.get().setPlayer(null);
		event.getPlayer().setOnline(false);
		garbage(optClient.get());
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		clear();
		EventManager.unregisterListener(this);
	}

	/**
	 * Gets or creates a client associated to the socket IP address and set its mumble address.
	 * 
	 * @param socket The socket used to send data to the remote TCP client.
	 * 
	 * @return The client associated to the IP address.
	 */
	private MumblePlayerClient createClient(ITcpConnection connection) {
		MumblePlayerClient client = getOrCreateClientByMumble(connection);
		client.createTcpClient(connection);
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
	private MumblePlayerClient getOrCreateClientByGame(InetSocketAddress socketAddress) {
		List<MumblePlayerClient> list = getClients(socketAddress.getAddress().getHostAddress());

		// No client registered
		if (list.isEmpty())
			return createClient(Origin.PLAYER_CONNECTED_IN_GAME, socketAddress);

		// Case 2: Clients registered
		for (MumblePlayerClient client : list) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (client.isAssociatedTo(socketAddress.getPort()))
				return client;
		}

		Optional<MumblePlayerClient> optClient = new GamePortAnalyzer(list).check(socketAddress);
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
	private MumblePlayerClient getOrCreateClientByMumble(ITcpConnection connection) {
		List<MumblePlayerClient> list = getClients(connection.getAddress().getAddress().getHostAddress());

		// No client registered
		if (list.isEmpty())
			return createClient(Origin.PLAYER_CONNECTED_IN_MUMBLE, connection.getAddress());

		// Case 2: Clients registered
		for (MumblePlayerClient client : list) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (client.isAssociatedTo(connection.getAddress().getPort()))
				return client;
		}

		Optional<MumblePlayerClient> optClient = new GamePortAnalyzer(list).check(null);
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
				for (MumblePlayerClient client : clients) {
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

	private MumblePlayerClient createClient(Origin origin, InetSocketAddress address) {
		MumblePlayerClient client = new MumblePlayerClient(server, createUUID());
		clients.add(client);
		EventManager.callEvent(new ServerClientAddPostEvent(server, client, origin, address));
		return client;
	}

	/**
	 * Thread safe operation to get the list of clients associated to the given name.
	 * 
	 * @param hostAddress The IP address without the port number
	 * 
	 * @return The list of registered clients.
	 */
	private List<MumblePlayerClient> getClients(String hostAddress) {
		List<MumblePlayerClient> list = new ArrayList<MumblePlayerClient>();
		lock.lock();
		try {
			for (MumblePlayerClient client : clients)
				if (client.getGameAddress().getAddress().getHostAddress().equals(hostAddress)
						|| client.getMumbleAddress().getAddress().getHostAddress().equals(hostAddress))
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
	private void removeClient(MumblePlayerClient client) {
		lock.lock();
		boolean removed = false;
		try {
			removed = clients.remove(client);
		} finally {
			lock.unlock();
		}

		if (removed)
			EventManager.callEvent(new ServerClientRemovePostEvent(server, client));
	}

	/**
	 * Removes the given client if and only if the player and the mumble client are disconnected.
	 * 
	 * @param client The client to remove.
	 */
	private void garbage(MumblePlayerClient client) {
		if (client.getPlayer() == null && (client.getTcpConnection() == null || client.getTcpConnection().isDisposed()))
			removeClient(client);
	}
}
