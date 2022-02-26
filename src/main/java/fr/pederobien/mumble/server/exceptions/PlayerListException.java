package fr.pederobien.mumble.server.exceptions;

import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;

public class PlayerListException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private IChannelPlayerList list;

	public PlayerListException(String message, IChannelPlayerList list) {
		super(message);
		this.list = list;
	}

	/**
	 * @return The list involved in this exception.
	 */
	public IChannelPlayerList getList() {
		return list;
	}
}
