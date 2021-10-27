package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerAdminStatusChangeEvent extends PlayerEvent {
	private boolean isMute;

	/**
	 * Creates an event thrown when the admin status of a player has changed.
	 * 
	 * @param player The player whose the admin status has changed.
	 * @param isMute The player admin status.
	 */
	public PlayerAdminStatusChangeEvent(IPlayer player, boolean isMute) {
		super(player);
		this.isMute = isMute;
	}

	/**
	 * @return The player admin status.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("mute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
