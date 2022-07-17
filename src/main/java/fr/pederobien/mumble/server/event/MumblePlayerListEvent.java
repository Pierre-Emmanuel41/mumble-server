package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;

public class MumblePlayerListEvent extends ProjectMumbleServerEvent {
	private IChannelPlayerList list;

	/**
	 * Creates a player list event.
	 * 
	 * @param list The list source involved in this event.
	 */
	public MumblePlayerListEvent(IChannelPlayerList list) {
		this.list = list;
	}

	/**
	 * @return The list involved in this event.
	 */
	public IChannelPlayerList getList() {
		return list;
	}
}
