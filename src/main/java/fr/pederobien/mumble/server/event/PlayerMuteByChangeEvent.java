package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerMuteByChangeEvent extends PlayerEvent {
	private IPlayer mutingPlayer;

	/**
	 * Creates an event thrown when a player has been muted by another player.
	 * 
	 * @param player       The player that is muted.
	 * @param mutingPlayer The player that is muting the previous player.
	 */
	public PlayerMuteByChangeEvent(IPlayer player, IPlayer mutingPlayer) {
		super(player);
		this.mutingPlayer = mutingPlayer;
	}

	/**
	 * @return The player that is muting the other player.
	 */
	public IPlayer getMutingPlayer() {
		return mutingPlayer;
	}
}
