package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerOnlineChangeEvent extends PlayerEvent {
	private boolean isOnline;

	/**
	 * Creates an event thrown when the online status of a player has changed.
	 * 
	 * @param player   The player whose the online status has changed.
	 * @param isOnline The player online status.
	 */
	public PlayerOnlineChangeEvent(IPlayer player, boolean isOnline) {
		super(player);
		this.isOnline = isOnline;
	}

	/**
	 * @return The player online status.
	 */
	public boolean isOnline() {
		return isOnline;
	}
}
