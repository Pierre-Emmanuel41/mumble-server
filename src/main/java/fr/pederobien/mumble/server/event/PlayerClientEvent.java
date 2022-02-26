package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.MumblePlayerClient;

public class PlayerClientEvent extends MumbleEvent {
	private MumblePlayerClient client;

	/**
	 * Creates a client event.
	 * 
	 * @param client The client source involved in this event.
	 */
	public PlayerClientEvent(MumblePlayerClient client) {
		this.client = client;
	}

	/**
	 * @return The client involved in this event.
	 */
	public MumblePlayerClient getClient() {
		return client;
	}
}
