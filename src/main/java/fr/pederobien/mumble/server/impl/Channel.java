package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelNameChangePreEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerAddPreEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerRemovePreEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePreEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.mumble.server.interfaces.ISoundModifier.VolumeResult;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class Channel implements IChannel, IEventListener {
	private static final double EPSILON = 1E-5;
	private IMumbleServer mumbleServer;
	private String name;
	private List<Player> players;
	private ISoundModifier soundModifier;
	private BlockingQueueTask<Dispatch> dispatcher;

	public Channel(IMumbleServer mumbleServer, String name) {
		this.mumbleServer = mumbleServer;
		this.name = name;
		soundModifier = AbstractSoundModifier.DEFAULT;
		players = new ArrayList<Player>();
		dispatcher = new BlockingQueueTask<>(String.format("%s-dispatcher", getName()), dispatch -> dispatch(dispatch));
		dispatcher.start();
		EventManager.registerListener(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addPlayer(IPlayer player) {
		EventManager.callEvent(new ChannelPlayerAddPreEvent(this, player), () -> {
			Player playerImpl = (Player) player;
			players.add(playerImpl);
			playerImpl.setChannel(this);
			EventManager.callEvent(new ChannelPlayerAddPostEvent(this, player));
		});
	}

	@Override
	public void removePlayer(IPlayer player) {
		if (!players.contains(player))
			return;

		EventManager.callEvent(new ChannelPlayerRemovePreEvent(this, player), () -> {
			Player playerImpl = (Player) player;
			playerImpl.setChannel(null);
			players.remove(player);
			EventManager.callEvent(new ChannelPlayerRemovePostEvent(this, player));
		});
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

		EventManager.callEvent(new ChannelSoundModifierChangePreEvent(this, soundModifier), () -> {
			ISoundModifier oldSoundModifier = this.soundModifier;
			this.soundModifier = soundModifier;
			EventManager.callEvent(new ChannelSoundModifierChangePostEvent(this, oldSoundModifier));
		});
	}

	@Override
	public String toString() {
		return "Channel={" + name + "}";
	}

	public void setName(String name) {
		if (this.name == name)
			return;

		EventManager.callEvent(new ChannelNameChangePreEvent(this, name), () -> {
			String oldName = new String(this.name);
			this.name = name;
			EventManager.callEvent(new ChannelNameChangePostEvent(this, oldName));
		});
	}

	public void onPlayerSpeak(Player player, byte[] data) {
		players.stream().filter(p -> p.equals(player)).forEach(p -> dispatcher.add(new Dispatch(player, p, data)));
	}

	public void dispatch(Dispatch dispatch) {
		// No need to send data to the player if he is deafen.
		// No need to send data to the player if the player is muted by the receiver
		if (dispatch.getReceiver().isDeafen() || dispatch.getTransmitter().isMuteBy(dispatch.getReceiver()))
			return;

		VolumeResult result = soundModifier.calculate(dispatch.getTransmitter(), dispatch.getReceiver());
		if (Math.abs(result.getGlobal()) < EPSILON)
			return;
		dispatch.getReceiver().onOtherPlayerSpeaker(dispatch.getTransmitter().getName(), dispatch.getData(), result.getGlobal(), result.getLeft(), result.getRight());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onServerClosing(ServerClosePostEvent event) {
		if (!event.getServer().equals(mumbleServer))
			return;

		clear();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	private void onChannelRemove(ServerChannelRemovePostEvent event) {
		if (!event.getChannel().equals(this))
			return;

		EventManager.unregisterListener(this);
	}

	private class Dispatch {
		private Player transmitter, receiver;
		private byte[] data;

		public Dispatch(Player transmitter, Player receiver, byte[] data) {
			this.transmitter = transmitter;
			this.receiver = receiver;
			this.data = data;
		}

		public Player getTransmitter() {
			return transmitter;
		}

		public Player getReceiver() {
			return receiver;
		}

		public byte[] getData() {
			return data;
		}
	}
}
