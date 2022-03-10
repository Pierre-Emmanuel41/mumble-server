package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.ICancellable;

public class PlayerOnlineChangePreEvent extends PlayerEvent implements ICancellable {
	private boolean isCancelled, newOnline;

	/**
	 * Creates an event thrown when the online status of a player is about to change.
	 * 
	 * @param player    The player whose the online status is about to change.
	 * @param newOnline The new player's online status.
	 */
	public PlayerOnlineChangePreEvent(IPlayer player, boolean newOnline) {
		super(player);
		this.newOnline = newOnline;
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
	 * @return The new player's online status.
	 */
	public boolean getNewOnline() {
		return newOnline;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("newOnline=" + getNewOnline());
		return String.format("%s_%s", getName(), joiner);
	}
}
