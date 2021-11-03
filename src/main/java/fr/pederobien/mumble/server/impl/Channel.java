package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
import fr.pederobien.mumble.server.impl.modifiers.AbstractSoundModifier;
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
	private List<Player> players;
	private ISoundModifier soundModifier;

	public Channel(IMumbleServer mumbleServer, String name) {
		this.mumbleServer = mumbleServer;
		this.name = name;
		soundModifier = AbstractSoundModifier.DEFAULT;
		players = new ArrayList<Player>();
		EventManager.registerListener(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addPlayer(IPlayer player) {
		Runnable add = () -> {
			Player playerImpl = (Player) player;
			players.add(playerImpl);
			playerImpl.setChannel(this);
		};
		EventManager.callEvent(new ChannelPlayerAddPreEvent(this, player), add, new ChannelPlayerAddPostEvent(this, player));
	}

	@Override
	public void removePlayer(IPlayer player) {
		if (!players.contains(player))
			return;

		Runnable remove = () -> {
			Player playerImpl = (Player) player;
			playerImpl.setChannel(null);
			players.remove(player);
		};
		EventManager.callEvent(new ChannelPlayerRemovePreEvent(this, player), remove, new ChannelPlayerRemovePostEvent(this, player));
	}

	@Override
	public List<IPlayer> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	@Override
	public void clear() {
		int size = players.size();
		for (int i = 0; i < size; i++)
			removePlayer(players.get(0));
	}

	@Override
	public ISoundModifier getSoundModifier() {
		return soundModifier;
	}

	@Override
	public void setSoundModifier(ISoundModifier soundModifier) {
		if (this.soundModifier.equals(soundModifier))
			return;

		ISoundModifier oldSoundModifier = this.soundModifier;
		Runnable set = () -> this.soundModifier = soundModifier;
		EventManager.callEvent(new ChannelSoundModifierChangePreEvent(this, soundModifier), set, new ChannelSoundModifierChangePostEvent(this, oldSoundModifier));
	}

	@Override
	public String toString() {
		return "Channel={" + name + "}";
	}

	public void setName(String name) {
		if (this.name == name)
			return;

		String oldName = this.name;
		EventManager.callEvent(new ChannelNameChangePreEvent(this, name), () -> this.name = name, new ChannelNameChangePostEvent(this, oldName));
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

		List<Player> receivers = players.stream().filter(p -> p.equals(event.getPlayer())).collect(Collectors.toList());

		for (Player receiver : receivers) {
			// No need to send data to the player if he is deafen.
			// No need to send data to the player if the player is muted by the receiver
			if (receiver.isDeafen() || ((Player) event.getPlayer()).isMuteBy(receiver))
				return;

			VolumeResult result = soundModifier.calculate(event.getPlayer(), receiver);
			if (Math.abs(result.getGlobal()) < EPSILON)
				return;

			receiver.onOtherPlayerSpeaker(event.getPlayer(), event.getData(), result.getGlobal(), result.getLeft(), result.getRight());
		}
	}
}
