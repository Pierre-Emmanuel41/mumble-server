package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerMumbleClientNotJoinedException extends PlayerException {
	private static final long serialVersionUID = 1L;

	public PlayerMumbleClientNotJoinedException(IPlayer player) {
		super(String.format("The mumble client associated to player %s has not joined the server yet", player.getName()), player);
	}
}
