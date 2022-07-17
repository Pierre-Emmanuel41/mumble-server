package fr.pederobien.mumble.server.event;

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
}
