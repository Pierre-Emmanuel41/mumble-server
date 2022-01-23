package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelNameChangePreEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerAddPreEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerRemovePreEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePreEvent;
import fr.pederobien.mumble.server.event.PlayerSpeakPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.impl.modifiers.SoundModifier;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.mumble.server.interfaces.ISoundModifier.VolumeResult;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Channel implements IChannel, IEventListener {
	private static final double EPSILON = 1E-5;
	private IMumbleServer mumbleServer;
	private String name;
	private List<IPlayer> players;
	private ISoundModifier soundModifier;
	private Lock lock;

	public Channel(IMumbleServer mumbleServer, String name, ISoundModifier soundModifier) {
		this.mumbleServer = mumbleServer;
		this.name = name;
		this.soundModifier = soundModifier;
		((SoundModifier) soundModifier).setChannel(this);
		players = new ArrayList<IPlayer>();
		lock = new ReentrantLock(true);

		EventManager.registerListener(this);
	}

	@Override
	public IMumbleServer getServer() {
		return mumbleServer;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (this.name == name)
			return;

		String oldName = this.name;
		EventManager.callEvent(new ChannelNameChangePreEvent(this, name), () -> this.name = name, new ChannelNameChangePostEvent(this, oldName));
	}

	@Override
	public void addPlayer(IPlayer player) {
		EventManager.callEvent(new ChannelPlayerAddPreEvent(this, player), () -> addPlayer((Player) player), new ChannelPlayerAddPostEvent(this, player));
	}

	@Override
	public void removePlayer(IPlayer player) {
		if (!players.contains(player))
			return;

		EventManager.callEvent(new ChannelPlayerRemovePreEvent(this, player), () -> removePlayer((Player) player), new ChannelPlayerRemovePostEvent(this, player));
	}

	@Override
	public List<IPlayer> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	@Override
	public void clear() {
		int size = players.size();
		lock.lock();
		try {
			for (int i = 0; i < size; i++)
				EventManager.callEvent(new ChannelPlayerRemovePostEvent(this, players.remove(0)));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ISoundModifier getSoundModifier() {
		return soundModifier;
	}

	@Override
	public void setSoundModifier(ISoundModifier soundModifier) {
		if (this.soundModifier.equals(soundModifier))
			return;

		ISoundModifier futur = soundModifier == null ? SoundManager.getDefaultSoundModifier() : soundModifier;
		SoundModifier oldSoundModifier = (SoundModifier) this.soundModifier;
		Runnable set = () -> {
			oldSoundModifier.setChannel(null);
			this.soundModifier = futur;
			((SoundModifier) this.soundModifier).setChannel(this);
		};
		ChannelSoundModifierChangePreEvent preEvent = new ChannelSoundModifierChangePreEvent(this, getSoundModifier(), futur);
		ChannelSoundModifierChangePostEvent postEvent = new ChannelSoundModifierChangePostEvent(this, oldSoundModifier);
		EventManager.callEvent(preEvent, set, postEvent);
	}

	@Override
	public String toString() {
		return "Channel={" + name + "}";
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		if (!event.getServer().equals(mumbleServer))
			return;

		clear();
	}

	@EventHandler
	private void onChannelRemove(ServerChannelRemovePostEvent event) {
		if (!event.getChannel().equals(this))
			return;

		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onPlayerSpeak(PlayerSpeakPostEvent event) {
		if (!players.contains(event.getPlayer()))
			return;

		Iterator<IPlayer> iterator;
		lock.lock();
		try {
			iterator = new ArrayList<IPlayer>(players).iterator();
		} finally {
			lock.unlock();
		}

		while (iterator.hasNext()) {
			IPlayer receiver = iterator.next();

			// No need to send data to the player if he is deafen.
			// No need to send data to the player if the player is muted by the receiver
			if (receiver.isDeafen() || ((Player) event.getPlayer()).isMuteBy(receiver))
				return;

			VolumeResult result = soundModifier.calculate(event.getPlayer(), receiver);
			if (Math.abs(result.getGlobal()) < EPSILON)
				return;

			((Player) receiver).onOtherPlayerSpeaker(event.getPlayer(), event.getData(), result.getGlobal(), result.getLeft(), result.getRight());
		}
	}

	/**
	 * Thread safe operation in order to add the given in the list of players.
	 * 
	 * @param player The player to add.
	 */
	private void addPlayer(Player player) {
		player.setChannel(this);
		lock.lock();
		try {
			players.add(player);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation in order to remove the given player from the list of players.
	 * 
	 * @param player The player to remove.
	 */
	private void removePlayer(Player player) {
		player.setChannel(null);
		lock.lock();
		try {
			players.remove(player);
		} finally {
			lock.unlock();
		}
	}
}
