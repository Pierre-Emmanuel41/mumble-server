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
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("list=" + getServer().getName());
		joiner.add("name=" + getPlayer().getName());
		joiner.add("gameAddress=" + getPlayer().getGameAddress().getAddress().getHostAddress());
		joiner.add("gamePort=" + getPlayer().getGameAddress().getPort());
		joiner.add("isAdmin=" + getPlayer().isAdmin());
		joiner.add("isMute=" + getPlayer().isMute());
		joiner.add("isDeafen=" + getPlayer().isDeafen());
		joiner.add("x=" + getPlayer().getPosition().getX());
		joiner.add("y=" + getPlayer().getPosition().getY());
		joiner.add("z=" + getPlayer().getPosition().getZ());
		joiner.add("yaw=" + getPlayer().getPosition().getYaw());
		joiner.add("pitch=" + getPlayer().getPosition().getPitch());
		return String.format("%s_%s", getName(), joiner);
	}
}
