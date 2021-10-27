package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.Client;

public class ClientDisconnectPostEvent extends ClientEvent {

	/**
	 * Creates an event thrown when a mumble client has been disconnected from the server.
	 * 
	 * @param client The disconnected client.
	 */
	public ClientDisconnectPostEvent(Client client) {
		super(client);
	}
}
