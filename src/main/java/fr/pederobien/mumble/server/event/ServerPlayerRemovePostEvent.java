package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;

public class ServerPlayerRemovePostEvent extends ServerPlayerListEvent {
	private IPlayer player;

	/**
	 * Creates an event thrown when a player has been removed from a server.
	 * 
	 * @param list   The list from which a player has been removed.
	 * @param player The removed player.
	 */
	public ServerPlayerRemovePostEvent(IServerPlayerList list, IPlayer player) {
		super(list);
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
		joiner.add("list=" + getList().getName());
		joiner.add("player=" + getPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
