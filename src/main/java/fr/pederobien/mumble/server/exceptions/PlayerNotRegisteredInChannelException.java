package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerNotRegisteredInChannelException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private IPlayer notRegisteredPlayer;

	public PlayerNotRegisteredInChannelException(IPlayer notRegisteredPlayer) {
		super("The player " + notRegisteredPlayer.getName() + " is not registered in a channel");
		this.notRegisteredPlayer = notRegisteredPlayer;
	}

	/**
	 * @return The player involved in this exception.
	 */
	public IPlayer getNotRegisteredPlayer() {
		return notRegisteredPlayer;
	}
}
