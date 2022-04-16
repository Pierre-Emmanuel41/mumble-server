package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPreEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePreEvent;
import fr.pederobien.mumble.server.exceptions.ServerPlayerListPlayerAlreadyRegisteredException;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class ServerPlayerList implements IServerPlayerList, IEventListener {
	private IMumbleServer server;
	private Map<String, IPlayer> players;
	private Lock lock;

	/**
	 * Creates a player list associated to the given server.
	 * 
	 * @param server The server to which this list is attached.
	 */
	public ServerPlayerList(IMumbleServer server) {
		this.server = server;

		players = new HashMap<String, IPlayer>();
		lock = new ReentrantLock(true);

		EventManager.registerListener(this);
	}

	@Override
	public Iterator<IPlayer> iterator() {
		return players.values().iterator();
	}

	@Override
	public IMumbleServer getServer() {
		return server;
	}

	@Override
	public String getName() {
		return server.getName();
	}

	@Override
	public IPlayer add(String name, InetSocketAddress gameAddress, boolean isAdmin, double x, double y, double z, double yaw, double pitch) {
		Optional<IPlayer> optPlayer = get(name);
		if (optPlayer.isPresent())
			throw new ServerPlayerListPlayerAlreadyRegisteredException(server.getPlayers(), optPlayer.get());

		IPlayer player = new Player(getServer(), name, gameAddress, isAdmin, x, y, z, yaw, pitch);
		ServerPlayerAddPreEvent preEvent = new ServerPlayerAddPreEvent(this, player);
		EventManager.callEvent(preEvent, () -> addPlayer(player));
		return preEvent.isCancelled() ? null : player;
	}

	@Override
	public IPlayer remove(String name) {
		Optional<IPlayer> optPlayer = get(name);
		if (!optPlayer.isPresent())
			return null;

		ServerPlayerRemovePreEvent preEvent = new ServerPlayerRemovePreEvent(this, optPlayer.get());
		EventManager.callEvent(preEvent, () -> removePlayer(name));
		return preEvent.isCancelled() ? null : optPlayer.get();
	}

	@Override
	public boolean remove(IPlayer player) {
		return remove(player.getName()) != null;
	}

	@Override
	public void clear() {
		lock.lock();
		try {
			Set<String> names = new HashSet<String>(players.keySet());
			for (String name : names) {
				EventManager.callEvent(new ServerPlayerRemovePreEvent(this, players.remove(name)));
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<IPlayer> get(String name) {
		return Optional.ofNullable(players.get(name));
	}

	@Override
	public Stream<IPlayer> stream() {
		return toList().stream();
	}

	@Override
	public List<IPlayer> toList() {
		return new ArrayList<IPlayer>(players.values());
	}

	@Override
	public List<IPlayer> getPlayersInChannel() {
		List<IPlayer> players = toList();
		players.removeIf(player -> player.getChannel() == null);
		return players;
	}

	@EventHandler
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		Optional<IPlayer> optOldPlayer = get(event.getOldName());
		if (!optOldPlayer.isPresent())
			return;

		Optional<IPlayer> optNewPlayer = get(event.getPlayer().getName());
		if (optNewPlayer.isPresent())
			throw new ServerPlayerListPlayerAlreadyRegisteredException(server.getPlayers(), optNewPlayer.get());

		lock.lock();
		try {
			players.put(event.getPlayer().getName(), players.remove(event.getOldName()));
		} finally {
			lock.unlock();
		}
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		EventManager.unregisterListener(this);
	}

	/**
	 * Thread safe operation that consists in adding the given player to this list.
	 * 
	 * @param player The player to add.
	 */
	private void addPlayer(IPlayer player) {
		lock.lock();
		try {
			players.put(player.getName(), player);
		} finally {
			lock.unlock();
		}

		EventManager.callEvent(new ServerPlayerAddPostEvent(this, player));
	}

	/**
	 * Thread safe operation that consists in removing the player associated to the given name.
	 * 
	 * @param name The name of the player to remove.
	 * 
	 * @return The remove player if registered, null otherwise.
	 */
	private IPlayer removePlayer(String name) {
		lock.lock();
		IPlayer player = null;
		try {
			player = players.remove(name);
		} finally {
			lock.unlock();
		}

		if (player != null)
			EventManager.callEvent(new ServerPlayerRemovePostEvent(this, player));

		return player;
	}
}
