package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerMuteChangeEvent extends PlayerEvent {
	private boolean isMute;

	/**
	 * Creates an event thrown when the mute status of a player has changed.
	 * 
	 * @param player The player whose the mute status has changed.
	 * @param isMute The player mute status.
	 */
	public PlayerMuteChangeEvent(IPlayer player, boolean isMute) {
		super(player);
		this.isMute = isMute;
	}

	/**
	 * @return The player mute status.
	 */
	public boolean isMute() {
		return isMute;
	}
}
