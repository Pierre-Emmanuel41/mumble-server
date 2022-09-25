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

import fr.pederobien.mumble.server.event.MumblePlayerKickPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerAddPreEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerRemovePreEvent;
import fr.pederobien.mumble.server.event.MumblePlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClosePostEvent;
import fr.pederobien.mumble.server.exceptions.PlayerAlreadyRegisteredException;
import fr.pederobien.mumble.server.exceptions.PlayerMumbleClientNotJoinedException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.vocal.server.event.VocalPlayerSpeakEvent;

public class ChannelPlayerList implements IChannelPlayerList, IEventListener {
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

		EventManager.registerListener(this);
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
		if (player.getChannel() != null)
			throw new PlayerAlreadyRegisteredException(player.getChannel().getPlayers(), player);

		lock.lock();
		try {
			if (get(player.getName()).isPresent())
				throw new PlayerAlreadyRegisteredException(this, player);

			Optional<PlayerMumbleClient> optClient = ((AbstractMumbleServer) channel.getServer()).getClients().get(player.getName());
			if (optClient.isPresent() && !optClient.get().isJoined())
				throw new PlayerMumbleClientNotJoinedException(player);

			Runnable update = () -> players.put(player.getName(), player);
			EventManager.callEvent(new MumblePlayerListPlayerAddPreEvent(this, player), update, new MumblePlayerListPlayerAddPostEvent(this, player));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public IPlayer remove(String name) {
		Optional<IPlayer> optPlayer = get(name);
		if (!optPlayer.isPresent())
			return null;

		EventManager.callEvent(new MumblePlayerListPlayerRemovePreEvent(this, optPlayer.get()), () -> removePlayer(optPlayer.get()));
		return optPlayer.get();
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
				EventManager.callEvent(new MumblePlayerListPlayerRemovePostEvent(this, players.remove(name)));
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
		return players.values().stream();
	}

	@Override
	public List<IPlayer> toList() {
		return new ArrayList<IPlayer>(players.values());
	}

	@EventHandler
	private void onPlayerNameChange(MumblePlayerNameChangePostEvent event) {
		Optional<IPlayer> optOldPlayer = get(event.getOldName());
		if (!optOldPlayer.isPresent())
			return;

		Optional<IPlayer> optNewPlayer = get(event.getPlayer().getName());
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
	private void onPlayerSpeak(VocalPlayerSpeakEvent event) {
		Optional<IPlayer> optPlayer = get(event.getTransmitter().getName());
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

	@EventHandler
	private void onPlayerKick(MumblePlayerKickPostEvent event) {
		if (!event.getChannel().equals(getChannel()))
			return;

		lock.lock();
		try {
			players.remove(event.getPlayer().getName());
		} finally {
			lock.unlock();
		}
	}

	@EventHandler
	private void onPlayerOnlineChange(MumblePlayerOnlineChangePostEvent event) {
		Optional<IPlayer> optPlayer = get(event.getPlayer().getName());
		if (!optPlayer.isPresent())
			return;

		removePlayer(optPlayer.get());
	}

	@EventHandler
	private void onChannelRemove(MumbleServerChannelRemovePostEvent event) {
		if (!event.getChannel().equals(getChannel()))
			return;

		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onServerClosing(MumbleServerClosePostEvent event) {
		if (!event.getServer().equals(getChannel().getServer()))
			return;

		EventManager.unregisterListener(this);
	}

	/**
	 * Thread safe operation that removes a players from the players list.
	 * 
	 * @param player The player to remove.
	 * 
	 * @return The player associated to the given name if registered, null otherwise.
	 */
	private boolean removePlayer(IPlayer player) {
		lock.lock();
		boolean removed;
		try {
			removed = players.remove(player.getName()) != null;
		} finally {
			lock.unlock();
		}

		if (removed)
			EventManager.callEvent(new MumblePlayerListPlayerRemovePostEvent(this, player));

		return removed;
	}
}
