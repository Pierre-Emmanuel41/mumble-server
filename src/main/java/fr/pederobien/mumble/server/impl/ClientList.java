package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.impl.TcpServerConnection;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.ClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.ServerClientCreatedEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPreEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePreEvent;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.LogEvent;

public class ClientList implements IEventListener {
	private InternalServer internalServer;
	private Map<String, List<Client>> clients;
	private Lock lock;

	public ClientList(InternalServer internalServer) {
		this.internalServer = internalServer;
		clients = new HashMap<String, List<Client>>();
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
	public Client createClient(Socket socket) {
		ITcpConnection connection = new TcpServerConnection(socket, new MessageExtractor());
		Client client = getOrCreateClientByMumble(connection);
		client.createTcpClient(connection);
		EventManager.callEvent(new LogEvent(String.format("Adding mumble client with address %s:%s", socket.getInetAddress().getHostAddress(), socket.getPort())));
		return client;
	}

	/**
	 * Get the list of clients associated to the given IP address.
	 * 
	 * @param address The IP address used to retrieve the list of associated clients.
	 * 
	 * @return The list that contains registered clients.
	 */
	public List<Client> getClients(InetAddress address) {
		return getClients(address.getHostAddress());
	}

	/**
	 * @return The list of player currently registered for this server.
	 */
	public List<IPlayer> getPlayers() {
		List<IPlayer> players = new ArrayList<IPlayer>();
		Iterator<Map.Entry<String, List<Client>>> iterator = getIterator();
		while (iterator.hasNext()) {
			List<Client> clientsList = iterator.next().getValue();
			for (Client client : clientsList)
				if (client.getPlayer() != null)
					players.add(client.getPlayer());
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
	public Optional<Client> getClient(String name) {
		Iterator<Map.Entry<String, List<Client>>> iterator = getIterator();
		while (iterator.hasNext()) {
			List<Client> clientsList = iterator.next().getValue();
			for (Client client : clientsList)
				if (client.getPlayer() != null && client.getPlayer().getName().equals(name))
					return Optional.of(client);
		}
		return Optional.empty();
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

		Client client = getOrCreateClientByGame(address);
		client.setGameAddress(address);
		client.setPlayer(optPlayer.get());
		optPlayer.get().setIsOnline(true);
		EventManager.callEvent(new LogEvent(String.format("Adding player with address %s:%s", address.getAddress().getHostAddress(), address.getPort())));
		return optPlayer.get();
	}

	/**
	 * Find the client associated to the given name and if it exists, then remove the player associated to the client.
	 * 
	 * @param name The player name.
	 * 
	 * @return True if the player has been removed, false otherwise.
	 */
	public boolean removePlayer(String name) {
		Optional<Client> optClient = getClient(name);
		if (optClient.isPresent() && optClient.get().getPlayer() != null) {
			Player player = optClient.get().getPlayer();
			ServerPlayerRemovePreEvent serverPlayerRemovePreEvent = new ServerPlayerRemovePreEvent(internalServer.getMumbleServer(), player);
			EventManager.callEvent(serverPlayerRemovePreEvent);
			if (serverPlayerRemovePreEvent.isCancelled())
				return false;

			player.setIsOnline(false);
			optClient.get().setPlayer(null);
			garbage(optClient.get());
			EventManager.callEvent(new ServerPlayerRemovePostEvent(internalServer.getMumbleServer(), player));
		}
		return false;
	}

	/**
	 * Try to find the player associated to the specified playerName.
	 * 
	 * @param playerName The name of the player to return.
	 * 
	 * @return An optional that contains a player if found, an empty optional otherwise.
	 */
	public Optional<Player> getPlayer(String name) {
		Optional<Client> optClient = getClient(name);
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
	public Optional<Client> getClient(InetAddress address, int port) {
		List<Client> clients = getClients(address.getHostAddress());

		if (clients == null)
			return Optional.empty();

		for (Client client : clients) {
			if (client.isAssociatedTo(port))
				return Optional.of(client);
		}
		return Optional.empty();
	}

	/**
	 * @return The list of registered clients.
	 */
	public List<Client> getClients() {
		List<Client> list = new ArrayList<Client>();
		Iterator<Entry<String, List<Client>>> iterator = getIterator();
		while (iterator.hasNext()) {
			List<Client> clientsList = iterator.next().getValue();
			for (Client client : clientsList)
				list.add(client);
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
	public void forEach(Consumer<? super Client> action) {
		getClients().forEach(action);
	}

	/**
	 * Removes each registered client on each registered IP address.
	 */
	public void clear() {
		lock.lock();
		try {
			for (List<Client> list : clients.values())
				list.clear();
			clients.clear();
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
	private List<Client> getOrCreateClients(String hostAddress) {
		List<Client> list = getClients(hostAddress);
		if (list == null) {
			list = Collections.synchronizedList(new ArrayList<Client>());
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
	private Client getOrCreateClientByGame(InetSocketAddress socketAddress) {
		InetAddress address = socketAddress.getAddress();
		List<Client> clients = getOrCreateClients(address.getHostAddress());

		// No client registered
		if (clients.isEmpty()) {
			Client client = createClient();
			clients.add(client);
			EventManager.callEvent(new LogEvent(String.format("Registering 1st client #%s for address %s", client.hashCode(), address.getHostAddress())));
			return client;
		}

		// Case 2: Clients registered
		for (Client client : clients) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (client.isAssociatedTo(socketAddress.getPort())) {
				EventManager.callEvent(new LogEvent(String.format("Client #%s associated to port n°%s", client.hashCode(), socketAddress.getPort())));
				return client;
			}

			// Verifying if the given port number is used on client side.
			if (new GamePort(client.getTcpClient().getConnection()).check(socketAddress)) {
				EventManager.callEvent(new LogEvent(String.format("Client #%s used port n°%s to play game", client.hashCode(), socketAddress.getPort())));
				return client;
			}
		}

		Client client = createClient();
		clients.add(client);
		EventManager.callEvent(
				new LogEvent(String.format("Registering new client #%s for address %s:%s", client.hashCode(), address.getHostAddress(), socketAddress.getPort())));
		return client;
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
	private Client getOrCreateClientByMumble(ITcpConnection connection) {
		InetSocketAddress address = connection.getAddress();
		List<Client> clients = getOrCreateClients(address.getAddress().getHostAddress());

		// No client registered
		if (clients.isEmpty()) {
			Client client = createClient();
			clients.add(client);
			EventManager.callEvent(new LogEvent(String.format("Registering 1st client #%s for address %s", client.hashCode(), address.getAddress().getHostAddress())));
			return client;
		}

		// Case 2: Clients registered
		for (Client client : clients) {
			// The game address or the mumble address correspond exactly to the IP address and port number.
			if (client.isAssociatedTo(connection.getAddress().getPort())) {
				EventManager.callEvent(new LogEvent(String.format("Client #%s associated to port n°%s", client.hashCode(), address.getPort())));
				return client;
			}

			// Verifying if the given port number is used on client side.
			if (new GamePort(connection).check(client.getGameAddress())) {
				EventManager.callEvent(new LogEvent(String.format("Client #%s used port n°%s to play game", client.hashCode(), client.getGameAddress().getPort())));
				return client;
			}
		}

		Client client = createClient();
		clients.add(client);
		EventManager.callEvent(
				new LogEvent(String.format("Registering new client #%s for address %s:%s", client.hashCode(), address.getAddress().getHostAddress(), address.getPort())));
		return client;
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
		Player player = new Player(internalServer, address, playerName, isAdmin);
		ServerPlayerAddPreEvent event = new ServerPlayerAddPreEvent(internalServer.getMumbleServer(), player);
		EventManager.callEvent(event);
		if (event.isCancelled())
			return Optional.empty();

		EventManager.callEvent(new ServerPlayerAddPostEvent(internalServer.getMumbleServer(), player));
		return Optional.of(player);
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
				for (List<Client> clients : clients.values()) {
					for (Client client : clients) {
						if (client.getUUID().equals(uuid)) {
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

	private Client createClient() {
		Client client = new Client(internalServer, createUUID());
		EventManager.callEvent(new ServerClientCreatedEvent(internalServer.getMumbleServer(), client));
		return client;
	}

	/**
	 * Thread safe operation to get the list of clients associated to the given name.
	 * 
	 * @param hostAddress The IP address without the port number
	 * 
	 * @return The list of registered clients.
	 */
	private List<Client> getClients(String hostAddress) {
		lock.lock();
		try {
			return clients.get(hostAddress);
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
	private void putClients(String hostAddress, List<Client> list) {
		lock.lock();
		try {
			clients.put(hostAddress, list);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation to iterate over a copy of the clients collection.
	 * 
	 * @return The iterator associated to the copy collection.
	 */
	private Iterator<Map.Entry<String, List<Client>>> getIterator() {
		lock.lock();
		try {
			return new HashMap<String, List<Client>>(clients).entrySet().iterator();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation to remove the given client from the registered clients list.
	 * 
	 * @param client The client to remove.
	 */
	private void removeClient(Client client) {
		EventManager.callEvent(new LogEvent(String.format("Removing client #%s", client.hashCode())));
		Iterator<Entry<String, List<Client>>> entryIterator = getIterator();
		while (entryIterator.hasNext()) {
			List<Client> clientsList = entryIterator.next().getValue();
			if (clientsList.contains(client))
				clientsList.remove(client);
		}
	}

	/**
	 * Removes the given client if and only if the player and the mumble client are disconnected.
	 * 
	 * @param client The client to remove.
	 */
	private void garbage(Client client) {
		if (client.getPlayer() == null && client.getTcpClient().getConnection().isDisposed())
			removeClient(client);
	}

	private class GamePort implements IEventListener {
		private ITcpConnection connection;
		private Lock lock;
		private Condition received;
		private boolean isUsed;

		public GamePort(ITcpConnection connection) {
			this.connection = connection;

			lock = new ReentrantLock();
			received = lock.newCondition();
			EventManager.registerListener(this);
		}

		/**
		 * Send synchronously a request to the client in order to check if the port of the specified address is used.
		 * 
		 * @param port The port to check.
		 * 
		 * @return True if the port is used on the client side, false otherwise.
		 */
		public boolean check(InetSocketAddress address) {
			lock.lock();
			try {
				connection.send(new MumbleCallbackMessage(MumbleMessageFactory.create(Idc.GAME_PORT, address.getPort()), args -> {
				}));
				if (!received.await(5000, TimeUnit.MILLISECONDS))
					isUsed = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
			return isUsed;
		}

		@EventHandler
		private void onDataReceived(DataReceivedEvent event) {
			if (!event.getConnection().equals(connection))
				return;

			IMessage<Header> response = MumbleMessageFactory.parse(event.getBuffer());
			if (response.getHeader().getIdc() != Idc.GAME_PORT && response.getHeader().getOid() != Oid.SET)
				return;

			isUsed = (boolean) response.getPayload()[1];
			EventManager.unregisterListener(this);
			lock.lock();
			try {
				received.signal();
			} finally {
				lock.unlock();
			}
		}
	}
}
