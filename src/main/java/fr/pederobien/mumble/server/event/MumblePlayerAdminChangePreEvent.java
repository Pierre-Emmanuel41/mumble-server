package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.ICancellable;

public class MumblePlayerAdminChangePreEvent extends MumblePlayerEvent implements ICancellable {
	private boolean isCancelled;
	private boolean newAdmin;

	/**
	 * Creates an event thrown when the administrator status of a player is about to change.
	 * 
	 * @param player   The player whose the administrator status is about to change.
	 * @param newAdmin The new player's administrator status.
	 */
	public MumblePlayerAdminChangePreEvent(IPlayer player, boolean newAdmin) {
		super(player);
		this.newAdmin = newAdmin;
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
	 * @return The new player's administrator status.
	 */
	public boolean getNewAdmin() {
		return newAdmin;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("currentAdmin=" + getPlayer().isAdmin());
		joiner.add("newAdmin=" + getNewAdmin());
		return String.format("%s_%s", getName(), joiner);
	}
}
