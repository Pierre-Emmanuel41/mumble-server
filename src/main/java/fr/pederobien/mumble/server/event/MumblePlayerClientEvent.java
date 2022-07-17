package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.impl.PlayerMumbleClient;

public class MumblePlayerClientEvent extends ProjectMumbleServerEvent {
	private PlayerMumbleClient client;

	/**
	 * Creates a client event.
	 * 
	 * @param client The client source involved in this event.
	 */
	public MumblePlayerClientEvent(PlayerMumbleClient client) {
		this.client = client;
	}

	/**
	 * @return The client involved in this event.
	 */
	public PlayerMumbleClient getClient() {
		return client;
	}
}
