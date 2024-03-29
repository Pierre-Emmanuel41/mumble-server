package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private IPlayer player;

	public PlayerException(String message, IPlayer player) {
		this.player = player;
	}

	/**
	 * @return The player source involved in this exception.
	 */
	public IPlayer getPlayer() {
		return player;
	}
}
