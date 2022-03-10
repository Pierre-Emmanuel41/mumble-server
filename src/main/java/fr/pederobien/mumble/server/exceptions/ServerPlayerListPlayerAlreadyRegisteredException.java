package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;

public class ServerPlayerListPlayerAlreadyRegisteredException extends ServerPlayerListException {
	private static final long serialVersionUID = 1L;
	private IPlayer player;

	public ServerPlayerListPlayerAlreadyRegisteredException(IServerPlayerList list, IPlayer player) {
		super(String.format("The player %s is already registered on %s", player.getName(), list.getName()), list);
		this.player = player;
	}

	/**
	 * @return The already registered player.
	 */
	public IPlayer getPlayer() {
		return player;
	}
}
