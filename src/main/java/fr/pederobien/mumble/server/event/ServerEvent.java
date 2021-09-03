package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerEvent extends MumbleEvent {
	private IMumbleServer server;

	/**
	 * Creates a server event.
	 * 
	 * @param server The server source involved in this event.
	 */
	public ServerEvent(IMumbleServer server) {
		this.server = server;
	}

	/**
	 * @return The server involved in this server.
	 */
	public IMumbleServer getServer() {
		return server;
	}
}
