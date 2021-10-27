package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.Client;

public class ClientEvent extends MumbleEvent {
	private Client client;

	/**
	 * Creates a client event.
	 * 
	 * @param client The client source involved in this event.
	 */
	public ClientEvent(Client client) {
		this.client = client;
	}

	/**
	 * @return The client involved in this event.
	 */
	public Client getClient() {
		return client;
	}
}
