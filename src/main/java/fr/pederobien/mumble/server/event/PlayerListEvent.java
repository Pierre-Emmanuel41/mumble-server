package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;

public class PlayerListEvent extends ProjectMumbleServerEvent {
	private IChannelPlayerList list;

	/**
	 * Creates a player list event.
	 * 
	 * @param list The list source involved in this event.
	 */
	public PlayerListEvent(IChannelPlayerList list) {
		this.list = list;
	}

	/**
	 * @return The list involved in this event.
	 */
	public IChannelPlayerList getList() {
		return list;
	}
}
