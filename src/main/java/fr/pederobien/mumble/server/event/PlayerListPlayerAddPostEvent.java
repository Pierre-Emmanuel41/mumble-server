package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;

public class PlayerListPlayerAddPostEvent extends PlayerListEvent {
	private IPlayer player;

	/**
	 * Creates an event thrown when a player has been added to a players list.
	 * 
	 * @param list   The list to which a player has been added.
	 * @param player The added player.
	 */
	public PlayerListPlayerAddPostEvent(IChannelPlayerList list, IPlayer player) {
		super(list);
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
		joiner.add("list=" + getList().getName());
		joiner.add("player=" + getPlayer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
