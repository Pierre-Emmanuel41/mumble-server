package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.MumblePlayerClient;

public class ClientDisconnectPostEvent extends PlayerClientEvent {

	/**
	 * Creates an event thrown when a mumble client has been disconnected from the server.
	 * 
	 * @param client The disconnected client.
	 */
	public ClientDisconnectPostEvent(MumblePlayerClient client) {
		super(client);
	}
}
