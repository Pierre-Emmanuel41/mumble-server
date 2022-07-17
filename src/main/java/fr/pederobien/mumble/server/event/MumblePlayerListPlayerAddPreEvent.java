package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.ICancellable;

public class MumblePlayerListPlayerAddPreEvent extends MumblePlayerListEvent implements ICancellable {
	private boolean isCancelled;
	private IPlayer player;

	/**
	 * Creates an event thrown when a player is about to be added to a channel.
	 * 
	 * @param list   The list to which a player is about to be added.
	 * @param player The added player.
	 */
	public MumblePlayerListPlayerAddPreEvent(IChannelPlayerList list, IPlayer player) {
		super(list);
		this.player = player;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
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
