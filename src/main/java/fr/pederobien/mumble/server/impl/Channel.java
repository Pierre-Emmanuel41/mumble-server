package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.mumble.server.interfaces.ISoundModifier.VolumeResult;
import fr.pederobien.mumble.server.interfaces.observers.IObsChannel;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;
import fr.pederobien.utils.BlockingQueueTask;
import fr.pederobien.utils.Observable;

public class Channel implements IChannel, IObsServer {
	private static final double EPSILON = 1E-5;
	private String name;
	private List<Player> players;
	private Observable<IObsChannel> observers;
	private ISoundModifier soundModifier;
	private BlockingQueueTask<Dispatch> dispatcher;

	public Channel(String name) {
		this.name = name;
		players = new ArrayList<Player>();
		observers = new Observable<IObsChannel>();
		soundModifier = AbstractSoundModifier.DEFAULT;
		dispatcher = new BlockingQueueTask<>(String.format("%s-dispatcher", getName()), dispatch -> dispatch(dispatch));
		dispatcher.start();
	}

	@Override
	public void addObserver(IObsChannel obs) {
		observers.addObserver(obs);
	}

	@Override
	public void removeObserver(IObsChannel obs) {
		observers.removeObserver(obs);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addPlayer(IPlayer player) {
		Player playerImpl = (Player) player;
		players.add(playerImpl);
		playerImpl.setChannel(this);
		notifyObservers(obs -> obs.onPlayerAdded(this, playerImpl));
	}

	@Override
	public void removePlayer(IPlayer player) {
		Player playerImpl = (Player) player;
		playerImpl.setChannel(null);
		if (players.remove(playerImpl))
			notifyObservers(obs -> obs.onPlayerRemoved(this, player));
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
		this.soundModifier = soundModifier;
	}

	@Override
	public void onChannelAdded(IChannel channel) {

	}

	@Override
	public void onChannelRemoved(IChannel channel) {

	}

	@Override
	public void onServerClosing() {
		clear();
	}

	@Override
	public String toString() {
		return "Channel={" + name + "}";
	}

	public void setName(String name) {
		if (this.name == name)
			return;

		String oldName = new String(this.name);
		this.name = name;
		notifyObservers(obs -> obs.onChannelRenamed(this, oldName, this.name));
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

	private void notifyObservers(Consumer<IObsChannel> consumer) {
		observers.notifyObservers(consumer);
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
