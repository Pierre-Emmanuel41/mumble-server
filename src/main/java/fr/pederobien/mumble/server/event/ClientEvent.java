package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.MumblePlayerClient;

public class ClientEvent extends MumbleEvent {
	private MumblePlayerClient mumblePlayerClient;

	/**
	 * Creates a client event.
	 * 
	 * @param mumblePlayerClient The client source involved in this event.
	 */
	public ClientEvent(MumblePlayerClient mumblePlayerClient) {
		this.mumblePlayerClient = mumblePlayerClient;
	}

	/**
	 * @return The client involved in this event.
	 */
	public MumblePlayerClient getClient() {
		return mumblePlayerClient;
	}
}
