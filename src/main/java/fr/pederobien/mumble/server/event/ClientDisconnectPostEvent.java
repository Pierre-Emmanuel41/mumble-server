package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.MumblePlayerClient;

public class ClientDisconnectPostEvent extends ClientEvent {

	/**
	 * Creates an event thrown when a mumble client has been disconnected from the server.
	 * 
	 * @param mumblePlayerClient The disconnected client.
	 */
	public ClientDisconnectPostEvent(MumblePlayerClient mumblePlayerClient) {
		super(mumblePlayerClient);
	}
}
