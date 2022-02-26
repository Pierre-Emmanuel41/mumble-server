package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;

public class PlayerAlreadyRegisteredException extends PlayerListException {
	private static final long serialVersionUID = 1L;
	private IPlayer player;

	public PlayerAlreadyRegisteredException(IChannelPlayerList list, IPlayer player) {
		super(String.format("A player %s is already registered", player.getName()), list);
		this.player = player;
	}

	/**
	 * @return The registered player.
	 */
	public IPlayer getPlayer() {
		return player;
	}
}
