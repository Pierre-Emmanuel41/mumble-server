package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ServerPlayerAddPostEvent extends ServerEvent {
	private IPlayer player;

	/**
	 * Creates an event thrown when a player has been added on a server.
	 * 
	 * @param server The server on which a player has been added.
	 * @param player The added player.
	 */
	public ServerPlayerAddPostEvent(IMumbleServer server, IPlayer player) {
		super(server);
		this.player = player;
	}

	/**
	 * @return The added player.
	 */
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("player=" + getPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
