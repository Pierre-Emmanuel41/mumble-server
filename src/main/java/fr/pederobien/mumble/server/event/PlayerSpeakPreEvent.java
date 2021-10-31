package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.ICancellable;

public class PlayerSpeakPreEvent extends PlayerEvent implements ICancellable {
	private boolean isCancelled;
	private byte[] data;

	/**
	 * Creates an event thrown when a player is about to speak to other player.
	 * 
	 * @param player The player that is speaking
	 * @param data   The bytes array that contains the audio sample.
	 */
	public PlayerSpeakPreEvent(IPlayer player, byte[] data) {
		super(player);
		this.data = data;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * @return The bytes array that contains the audio sample.
	 */
	public byte[] getData() {
		return data;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("channel=" + getPlayer().getChannel().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
