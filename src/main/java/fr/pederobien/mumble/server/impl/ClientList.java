package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.server.event.ClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.ServerClientAddPostEvent;
import fr.pederobien.mumble.server.event.ServerClientAddPostEvent.Origin;
import fr.pederobien.mumble.server.event.ServerClientRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPreEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePreEvent;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class ClientList implements IEventListener {
	private InternalServer internalServer;
	private Map<String, List<MumblePlayerClient>> mumblePlayerClients;
	private Map<String, MumblePlayerClient> players;
	private Lock lock;

	public ClientList(InternalServer internalServer) {
		this.internalServer = internalServer;
		mumblePlayerClients = new HashMap<String, List<MumblePlayerClient>>();
		players = new HashMap<String, MumblePlayerClient>();
		lock = new ReentrantLock(true);

		EventManager.registerListener(this);
	}

	/**
	 * Gets or creates a client associated to the socket IP address and set its mumble address.
	 * 
	 * @param socket The socket used to send data to the remote TCP client.
	 * 
	 * @return The client associated to the IP address.
	 */
	public MumblePlayerClient createClient(ITcpConnection connection) {
		MumblePlayerClient mumblePlayerClient = getOrCreateClientByMumble(connection);
		mumblePlayerClient.createTcpClient(connection);
		return mumblePlayerClient;
	}

	/**
	 * Get the list of clients associated to the given IP address.
	 * 
	 * @param address The IP address used to retrieve the list of associated clients.
	 * 
	 * @return The list that contains registered clients.
	 */
	public List<MumblePlayerClient> getClients(InetAddress address) {
		return getClients(address.getHostAddress());
	}

	/**
	 * @return The list of player currently registered for this server.
	 */
	public List<IPlayer> getPlayers() {
		List<IPlayer> players = new ArrayList<IPlayer>();
		Iterator<Map.Entry<String, List<MumblePlayerClient>>> iterator = getIterator();
		while (iterator.hasNext()) {
			List<MumblePlayerClient> clientsList = iterator.next().getValue();
			for (MumblePlayerClient mumblePlayerClient : clientsList)
				if (mumblePlayerClient.getPlayer() != null)
					players.add(mumblePlayerClient.getPlayer());
		}
		return players;
	}

	/**
	 * Get the client associated to the given name.
	 * 
	 * @param name The player name.
	 * 
	 * @return An optional that contains the client associated to the specified name if registered, an empty optional otherwise.
	 */
	public Optional<MumblePlayerClient> getClient(String name) {
		return Optional.ofNullable(players.get(name));
	}

	/**
	 * Creates a player object based on the given parameters.
	 * 
	 * @param address    The IP address used by the player to play to the game.
	 * @param playerName The player name.
	 * @param isAdmin    The player admin status in game.
	 * 
	 * @return The created player if it was possible to create one, null otherwise.
	 */
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		Optional<Player> optPlayer = createPlayer(address, playerName, isAdmin);

		// The creation of a player has been cancelled.
		if (!optPlayer.isPresent())
			return null;

		MumblePlayerClient mumblePlayerClient = getOrCreateClientByGame(address);
		mumblePlayerClient.setGameAddress(address);
		mumblePlayerClient.setPlayer(optPlayer.get());
		optPlayer.get().setIsOnline(true);
		players.put(playerName, mumblePlayerClient);
		return optPlayer.get();
	}

	/**
	 * Find the client associated to the given name and if it exists, then remove the player associated to the client.
	 * 
	 * @param name The player name.
	 */
	public IPlayer removePlayer(String name) {
		Optional<MumblePlayerClient> optClient = getClient(name);
		if (optClient.isPresent() && optClient.get().getPlayer() != null) {
			Player player = optClient.get().getPlayer();
			Runnable remove = () -> {
				player.setIsOnline(false);
				optClient.get().setPlayer(null);
				garbage(optClient.get());
				players.remove(name);
			};

			ServerPlayerRemovePreEvent preEvent = new ServerPlayerRemovePreEvent(internalServer, player);
			ServerPlayerRemovePostEvent postEvent = new ServerPlayerRemovePostEvent(internalServer, player);
			EventManager.callEvent(preEvent, remove, postEvent);
			return player;
		}
		return null;
	}

	/**
	 * Try to find the player associated to the specified playerName.
	 * 
	 * @param playerName The name of the player to return.
	 * 
	 * @return An optional that contains a player if found, an empty optional otherwise.
	 */
	public Optional<Player> getPlayer(String name) {
		Optional<MumblePlayerClient> optClient = getClient(name);
		if (optClient.isPresent())
			return Optional.of(optClient.get().getPlayer());
		return Optional.empty();
	}

	/**
	 * Get the client associated to the given address and port number. It iterates over the registered client lists associated to the
	 * given address and check if the game address or the mumble address is associated to the given port number.
	 * 
	 * @param address The address used to get the associated client list.
	 * @param port    The port used to get a specific client.
	 * @return An optional that contains the client if registered, an empty optional otherwise.
	 */
	public Optional<MumblePlayerClient> getClient(InetAddress address, int port) {
		List<MumblePlayerClient> mumblePlayerClients = getClients(address.getHostAddress());

		if (mumblePlayerClients == null)
			return Optional.empty();

		for (MumblePlayerClient mumblePlayerClient : mumblePlayerClients) {
			if (mumblePlayerClient.isAssociatedTo(port))
				return Optional.of(mumblePlayerClient);
		}
		return Optional.empty();
	}

	/**
	 * @return The list of registered clients.
	 */
	public List<MumblePlayerClient> getClients() {
		List<MumblePlayerClient> list = new ArrayList<MumblePlayerClient>();
		Iterator<Entry<String, List<MumblePlayerClient>>> iterator = getIterator();
		while (iterator.hasNext()) {
			List<MumblePlayerClient> clientsList = iterator.next().getValue();
			for (MumblePlayerClient mumblePlayerClient : clientsList)
				list.add(mumblePlayerClient);
		}
		return list;
	}

	/**
	 * Performs the given action for each element of the {@code Iterable} until all elements have been processed or the action throws
	 * an exception. Unless otherwise specified by the implementing class, actions are performed in the order of iteration (if an
	 * iteration order is specified). Exceptions thrown by the action are relayed to the caller.
	 *
	 * @param action The action to be performed for each element.
	 * @throws NullPointerException if the specified action is null
	 */
	public void forEach(Consumer<? super MumblePlayerClient> action) {
		getClients().forEach(action);
	}

	/**
	 * Removes each registered client on each registered IP address.
	 */
	public void clear() {
		lock.lock();
		try {
			for (List<MumblePlayerClient> list : mumblePlayerClients.values())
				list.clear();
			mumblePlayerClients.clear();
		} finally {
			lock.unlock();
		}
	}

	@EventHandler
	private void onClientDisconnected(ClientDisconnectPostEvent event) {
		garbage(event.getClient());
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		clear();
		EventManager.unregisterListener(this);
	}

	/**
	 * Thread safe operation to get or to create a list of clients associated to the given host address. The returned list is
	 * synchronized.
	 * 
	 * @param hostAddress The host used as key to get the associated client list.
	 * 
	 * @return The list that contains the clients associated to the specified host address.
	 */
	private List<MumblePlayerClient> getOrCreateClients(String hostAddress) {
		List<MumblePlayerClient> list = getClients(hostAddress);
		if (list == null) {
			list = Collections.synchronizedList(new ArrayList<MumblePlayerClient>());
			putClients(hostAddress, list);
		}
		return list;
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
		InetAddress address = socketAddress.getAddress();
		List<MumblePlayerClient> mumblePlayerClients = getOrCreateClients(address.getHostAddress());

		// No client registered
		if (mumblePlayerClients.isEmpty()) {
			MumblePlayerClient mumblePlayerClient = createClient(Origin.PLAYER_CONNECTED_IN_GAME, socketAddress);
			mumblePlayerClients.add(mumblePlayerClient);
			return mumblePlayerClient;
		}

		// Case 2: Clients registered
		for (MumblePlayerClient mumblePlayerClient : mumblePlayerClients) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (mumblePlayerClient.isAssociatedTo(socketAddress.getPort()))
				return mumblePlayerClient;
		}

		Optional<MumblePlayerClient> optClient = new GamePortAnalyzer(mumblePlayerClients, null).check(socketAddress);
		if (optClient.isPresent())
			return optClient.get();

		MumblePlayerClient mumblePlayerClient = createClient(Origin.PLAYER_CONNECTED_IN_GAME, socketAddress);
		mumblePlayerClients.add(mumblePlayerClient);
		return mumblePlayerClient;
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
		InetSocketAddress address = connection.getAddress();
		List<MumblePlayerClient> mumblePlayerClients = getOrCreateClients(address.getAddress().getHostAddress());

		// No client registered
		if (mumblePlayerClients.isEmpty()) {
			MumblePlayerClient mumblePlayerClient = createClient(Origin.PLAYER_CONNECTED_IN_MUMBLE, connection.getAddress());
			mumblePlayerClients.add(mumblePlayerClient);
			return mumblePlayerClient;
		}

		// Case 2: Clients registered
		for (MumblePlayerClient mumblePlayerClient : mumblePlayerClients) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (mumblePlayerClient.isAssociatedTo(connection.getAddress().getPort()))
				return mumblePlayerClient;
		}

		Optional<MumblePlayerClient> optClient = new GamePortAnalyzer(mumblePlayerClients, connection).check(null);
		if (optClient.isPresent())
			return optClient.get();

		MumblePlayerClient mumblePlayerClient = createClient(Origin.PLAYER_CONNECTED_IN_MUMBLE, connection.getAddress());
		mumblePlayerClients.add(mumblePlayerClient);
		return mumblePlayerClient;
	}

	/**
	 * Creates the player associated to the given parameter.
	 * 
	 * @param address    The IP address used to play games.
	 * @param playerName The player name.
	 * @param isAdmin    The player admin status in game.
	 * 
	 * @return An optional that contains the created player if it was possible to create one, an empty optional otherwise.
	 */
	private Optional<Player> createPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		Player player = new Player(address, playerName, isAdmin);

		ServerPlayerAddPreEvent preEvent = new ServerPlayerAddPreEvent(internalServer, player);
		ServerPlayerAddPostEvent postEvent = new ServerPlayerAddPostEvent(internalServer, player);
		return Optional.ofNullable(EventManager.callEvent(preEvent, () -> player, ignored -> postEvent));
	}

	/**
	 * @return A unique identifier.
	 */
	private UUID createUUID() {
		UUID uuid;
		if (mumblePlayerClients.isEmpty())
			uuid = UUID.randomUUID();
		else {
			boolean uuidAlreadyExists = false;
			do {
				uuid = UUID.randomUUID();
				for (List<MumblePlayerClient> mumblePlayerClients : mumblePlayerClients.values()) {
					for (MumblePlayerClient mumblePlayerClient : mumblePlayerClients) {
						if (mumblePlayerClient.getUUID().equals(uuid)) {
							uuidAlreadyExists = true;
							break;
						}
					}
					if (uuidAlreadyExists)
						break;
				}
			} while (uuidAlreadyExists);
		}
		return uuid;
	}

	private MumblePlayerClient createClient(Origin origin, InetSocketAddress address) {
		MumblePlayerClient mumblePlayerClient = new MumblePlayerClient(internalServer, createUUID());
		EventManager.callEvent(new ServerClientAddPostEvent(internalServer, mumblePlayerClient, origin, address));
		return mumblePlayerClient;
	}

	/**
	 * Thread safe operation to get the list of clients associated to the given name.
	 * 
	 * @param hostAddress The IP address without the port number
	 * 
	 * @return The list of registered clients.
	 */
	private List<MumblePlayerClient> getClients(String hostAddress) {
		lock.lock();
		try {
			return mumblePlayerClients.get(hostAddress);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation to register a list of clients.
	 * 
	 * @param hostAddress The IP address without the port number.
	 * @param list        The clients to register.
	 */
	private void putClients(String hostAddress, List<MumblePlayerClient> list) {
		lock.lock();
		try {
			mumblePlayerClients.put(hostAddress, list);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation to iterate over a copy of the clients collection.
	 * 
	 * @return The iterator associated to the copy collection.
	 */
	private Iterator<Map.Entry<String, List<MumblePlayerClient>>> getIterator() {
		lock.lock();
		try {
			return new HashMap<String, List<MumblePlayerClient>>(mumblePlayerClients).entrySet().iterator();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation to remove the given client from the registered clients list.
	 * 
	 * @param mumblePlayerClient The client to remove.
	 */
	private void removeClient(MumblePlayerClient mumblePlayerClient) {
		Iterator<Entry<String, List<MumblePlayerClient>>> entryIterator = getIterator();
		while (entryIterator.hasNext()) {
			List<MumblePlayerClient> clientsList = entryIterator.next().getValue();
			if (clientsList.contains(mumblePlayerClient))
				clientsList.remove(mumblePlayerClient);
		}
		EventManager.callEvent(new ServerClientRemovePostEvent(internalServer, mumblePlayerClient));
	}

	/**
	 * Removes the given client if and only if the player and the mumble client are disconnected.
	 * 
	 * @param mumblePlayerClient The client to remove.
	 */
	private void garbage(MumblePlayerClient mumblePlayerClient) {
		if (mumblePlayerClient.getPlayer() == null && (mumblePlayerClient.getTcpClient() == null || mumblePlayerClient.getTcpClient().getConnection().isDisposed()))
			removeClient(mumblePlayerClient);
	}
}
