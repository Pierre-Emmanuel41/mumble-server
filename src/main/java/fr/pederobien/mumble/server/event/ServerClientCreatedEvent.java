package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.impl.Client;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerClientCreatedEvent extends ServerEvent {
	private Client client;

	/**
	 * Creates an event thrown when a new client has been created.
	 * 
	 * @param server The server on which the client has been created.
	 * @param client The created client.
	 */
	public ServerClientCreatedEvent(IMumbleServer server, Client client) {
		super(server);
		this.client = client;
	}

	/**
	 * @return The created client.
	 */
	public Client getClient() {
		return client;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("client=" + getClient().hashCode());
		return String.format("%s_%s", getName(), joiner);
	}
}
