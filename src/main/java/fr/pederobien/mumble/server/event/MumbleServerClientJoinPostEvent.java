package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.impl.PlayerMumbleClient;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class MumbleServerClientJoinPostEvent extends MumbleServerEvent {
	private PlayerMumbleClient client;

	/**
	 * Creates an event thrown when a player has joined a mumble server.
	 * 
	 * @param server The server joined by the client.
	 * @param client The client that has joined a server.
	 */
	public MumbleServerClientJoinPostEvent(IMumbleServer server, PlayerMumbleClient client) {
		super(server);
		this.client = client;
	}

	/**
	 * @return The client that has joined a server.
	 */
	public PlayerMumbleClient getClient() {
		return client;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("client=#" + getClient().hashCode());
		return String.format("%s_%s", getName(), joiner);
	}

}
