package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerEvent extends ProjectMumbleServerEvent {
	private IPlayer player;

	/**
	 * Creates a player event.
	 * 
	 * @param player The player source involved in this event.
	 */
	public PlayerEvent(IPlayer player) {
		this.player = player;
	}

	/**
	 * @return The player involved in this event.
	 */
	public IPlayer getPlayer() {
		return player;
	}
}
