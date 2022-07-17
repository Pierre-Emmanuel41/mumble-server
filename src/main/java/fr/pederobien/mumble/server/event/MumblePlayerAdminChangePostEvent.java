package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumblePlayerAdminChangePostEvent extends MumblePlayerEvent {
	private boolean oldAdmin;

	/**
	 * Creates an event thrown when the administrator status of a player has changed.
	 * 
	 * @param player   The player whose the administrator status has changed.
	 * @param oldAdmin The old player's administrator status.
	 */
	public MumblePlayerAdminChangePostEvent(IPlayer player, boolean oldAdmin) {
		super(player);
		this.oldAdmin = oldAdmin;
	}

	/**
	 * @return The old player's administrator status.
	 */
	public boolean getOldAdmin() {
		return oldAdmin;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("currentAdmin=" + getPlayer().isAdmin());
		joiner.add("newAdmin=" + getOldAdmin());
		return String.format("%s_%s", getName(), joiner);
	}
}
