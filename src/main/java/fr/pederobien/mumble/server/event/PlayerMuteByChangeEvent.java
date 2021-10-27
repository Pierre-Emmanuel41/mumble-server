package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerMuteByChangeEvent extends PlayerEvent {
	private IPlayer mutingPlayer;
	private boolean isMute;

	/**
	 * Creates an event thrown when a player has been muted by another player.
	 * 
	 * @param player       The player that is muted.
	 * @param mutingPlayer The player that is muting the previous player.
	 */
	public PlayerMuteByChangeEvent(IPlayer player, IPlayer mutingPlayer, boolean isMute) {
		super(player);
		this.mutingPlayer = mutingPlayer;
		this.isMute = isMute;
	}

	/**
	 * @return The player that is muting the other player.
	 */
	public IPlayer getMutingPlayer() {
		return mutingPlayer;
	}

	/**
	 * @return The new muteBy status of the player.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("mutingPlayer=" + getMutingPlayer().getName());
		joiner.add("mute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
