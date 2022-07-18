package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.impl.PlayerMumbleClient;

public class MumbleClientDisconnectPostEvent extends MumblePlayerClientEvent {

	/**
	 * Creates an event thrown when a mumble client has been disconnected from the server.
	 * 
	 * @param client The disconnected client.
	 */
	public MumbleClientDisconnectPostEvent(PlayerMumbleClient client) {
		super(client);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getClient().getServer().getName());
		joiner.add("client=#" + getClient().hashCode());
		return String.format("%s_%s", getName(), joiner);
	}
}
