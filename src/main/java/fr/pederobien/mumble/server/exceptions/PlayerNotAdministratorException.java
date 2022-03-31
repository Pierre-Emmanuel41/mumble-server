package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerNotAdministratorException extends PlayerException {
	private static final long serialVersionUID = 1L;

	public PlayerNotAdministratorException(IPlayer player) {
		super(String.format("The player %s must be an administrator", player.getName()), player);
	}
}
