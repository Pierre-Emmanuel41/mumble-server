package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerOpenPostEvent extends ServerEvent {

	/**
	 * Creates an event when a server has been opened.
	 * 
	 * @param server The opened server.
	 */
	public ServerOpenPostEvent(IMumbleServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getServer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
