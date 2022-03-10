package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.event.PlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.exceptions.PlayerAlreadyRegisteredException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.vocal.server.event.PlayerSpeakEvent;

public class ChannelPlayerList implements IChannelPlayerList {
	private IChannel channel;
	private Map<String, IPlayer> players;
	private Lock lock;

	/**
	 * Creates a player list associated to a channel.
	 * 
	 * @param channel The channel associated to this list.
	 */
	public ChannelPlayerList(IChannel channel) {
		this.channel = channel;
		players = new LinkedHashMap<String, IPlayer>();
		lock = new ReentrantLock(true);
	}

	@Override
	public Iterator<IPlayer> iterator() {
		return players.values().iterator();
	}

	@Override
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public String getName() {
		return channel.getName();
	}

	@Override
	public void add(IPlayer player) {
		addPlayer(player);
		EventManager.callEvent(new PlayerListPlayerAddPostEvent(this, player));
	}

	@Override
	public IPlayer remove(String name) {
		IPlayer player = removePlayer(name);
		if (player != null)
			EventManager.callEvent(new PlayerListPlayerRemovePostEvent(this, player));
		return player;
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
			for (String name : names)
				EventManager.callEvent(new PlayerListPlayerRemovePostEvent(this, players.remove(name)));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<IPlayer> getPlayer(String name) {
		return Optional.ofNullable(players.get(name));
	}

	@Override
	public Stream<IPlayer> stream() {
		return players.values().stream();
	}

	@Override
	public List<IPlayer> toList() {
		return new ArrayList<IPlayer>(players.values());
	}

	@EventHandler
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		Optional<IPlayer> optOldPlayer = getPlayer(event.getOldName());
		if (!optOldPlayer.isPresent())
			return;

		Optional<IPlayer> optNewPlayer = getPlayer(event.getPlayer().getName());
		if (optNewPlayer.isPresent())
			throw new PlayerAlreadyRegisteredException(this, optNewPlayer.get());

		lock.lock();
		try {
			players.remove(event.getOldName());
			players.put(event.getPlayer().getName(), event.getPlayer());
		} finally {
			lock.unlock();
		}
	}

	@EventHandler
	private void onPlayerSpeak(PlayerSpeakEvent event) {
		Optional<IPlayer> optPlayer = getPlayer(event.getTransmitter().getName());
		if (!optPlayer.isPresent())
			return;

		Iterator<IPlayer> iterator;
		lock.lock();
		try {
			iterator = toList().iterator();
		} finally {
			lock.unlock();
		}

		while (iterator.hasNext()) {
			IPlayer receiver = iterator.next();

			// No need to send data to the player if he is deafen.
			// No need to send data to the player if the player is muted by the receiver
			if (receiver.isDeafen() || ((Player) optPlayer.get()).isMuteBy(receiver))
				return;

			event.getVolumes().put(event.getPlayers().get(optPlayer.get().getName()), channel.getSoundModifier().calculate(optPlayer.get(), receiver));
		}
	}

	/**
	 * Thread safe operation that adds a player to the players list.
	 * 
	 * @param player The player to add.
	 * 
	 * @throws PlayerAlreadyRegisteredException if a player is already registered for the player name.
	 */
	private void addPlayer(IPlayer player) {
		lock.lock();
		try {
			if (players.get(player.getName()) != null)
				throw new PlayerAlreadyRegisteredException(this, player);

			players.put(player.getName(), player);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation that removes a players from the players list.
	 * 
	 * @param player The player to remove.
	 * 
	 * @return The player associated to the given name if registered, null otherwise.
	 */
	private IPlayer removePlayer(String name) {
		lock.lock();
		try {
			return players.remove(name);
		} finally {
			lock.unlock();
		}
	}
}
