package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class MumbleServerClosePostEvent extends MumbleServerEvent {

	/**
	 * Creates an event thrown when a server has been closed.
	 * 
	 * @param server The closed server.
	 */
	public MumbleServerClosePostEvent(IMumbleServer server) {
		super(server);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getServer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
