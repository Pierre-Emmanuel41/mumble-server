package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerClosePostEvent extends ServerEvent {

	/**
	 * Creates an event thrown when a server has been closed.
	 * 
	 * @param server The closed server.
	 */
	public ServerClosePostEvent(IMumbleServer server) {
		super(server);
	}
}
