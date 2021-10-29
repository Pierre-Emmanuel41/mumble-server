package fr.pederobien.mumble.server.event;

import java.net.InetSocketAddress;
import java.util.StringJoiner;

import fr.pederobien.mumble.server.impl.Client;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class ServerClientCreatedEvent extends ServerEvent {
	private Client client;
	private Origin origin;
	private InetSocketAddress address;

	/**
	 * Creates an event thrown when a new client has been created.
	 * 
	 * @param server The server on which the client has been created.
	 * @param client The created client.
	 */
	public ServerClientCreatedEvent(IMumbleServer server, Client client, Origin origin, InetSocketAddress address) {
		super(server);
		this.client = client;
		this.origin = origin;
		this.address = address;
	}

	/**
	 * @return The created client.
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @return The event at the origin of the creation of a new client.
	 */
	public Origin getOrigin() {
		return origin;
	}

	/**
	 * @return The address used to create a new client.
	 */
	public InetSocketAddress getAddress() {
		return address;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("client=#" + getClient().hashCode());
		joiner.add("origin=" + getOrigin());
		joiner.add("address=" + getAddress());
		return String.format("%s_%s", getName(), joiner);
	}

	public enum Origin {
		PLAYER_CONNECTED_IN_GAME, PLAYER_CONNECTED_IN_MUMBLE
	}
}
