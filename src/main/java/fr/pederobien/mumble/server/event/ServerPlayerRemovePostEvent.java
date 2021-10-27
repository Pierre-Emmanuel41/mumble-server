package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class ServerPlayerRemovePostEvent extends ServerEvent {
	private IPlayer player;

	/**
	 * Creates an event thrown when a player has been removed from a server.
	 * 
	 * @param server The server from which a player has been removed.
	 * @param player The removed player.
	 */
	public ServerPlayerRemovePostEvent(IMumbleServer server, IPlayer player) {
		super(server);
		this.player = player;
	}

	/**
	 * @return The removed player.
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
