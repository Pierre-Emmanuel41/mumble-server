package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

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

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("online=" + isOnline());
		return String.format("%s_%s", getName(), joiner);
	}
}
