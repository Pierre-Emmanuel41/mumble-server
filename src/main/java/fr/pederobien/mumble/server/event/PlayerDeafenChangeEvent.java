package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerDeafenChangeEvent extends PlayerEvent {
	private boolean isDeafen;

	/**
	 * Creates an event thrown when the deafen status of a player has changed.
	 * 
	 * @param player   The player whose the deafen status has changed.
	 * @param isDeafen The player deafen status.
	 */
	public PlayerDeafenChangeEvent(IPlayer player, boolean isDeafen) {
		super(player);
		this.isDeafen = isDeafen;
	}

	/**
	 * @return The player deafen status.
	 */
	public boolean isDeafen() {
		return isDeafen;
	}
}
