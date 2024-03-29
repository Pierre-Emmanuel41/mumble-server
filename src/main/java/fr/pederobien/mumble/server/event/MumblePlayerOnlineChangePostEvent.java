package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumblePlayerOnlineChangePostEvent extends MumblePlayerEvent {
	private boolean oldOnline;

	/**
	 * Creates an event thrown when the online status of a player has changed.
	 * 
	 * @param player    The player whose the online status has changed.
	 * @param oldOnline The old player's online status.
	 */
	public MumblePlayerOnlineChangePostEvent(IPlayer player, boolean oldOnline) {
		super(player);
		this.oldOnline = oldOnline;
	}

	/**
	 * @return The player online status.
	 */
	public boolean getOldOnline() {
		return oldOnline;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("currentOnline=" + getPlayer().isOnline());
		joiner.add("oldOnline=" + getOldOnline());
		return String.format("%s_%s", getName(), joiner);
	}
}
