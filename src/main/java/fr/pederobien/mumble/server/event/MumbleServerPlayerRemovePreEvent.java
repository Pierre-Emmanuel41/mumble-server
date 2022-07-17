package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;
import fr.pederobien.utils.ICancellable;

public class MumbleServerPlayerRemovePreEvent extends MumbleServerPlayerListEvent implements ICancellable {
	private boolean isCancelled;
	private IPlayer player;

	/**
	 * Creates an event thrown when a player is about to be removed from a server.
	 * 
	 * @param list   The list from which a player is about to be removed.
	 * @param player The removed player.
	 */
	public MumbleServerPlayerRemovePreEvent(IServerPlayerList list, IPlayer player) {
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
	 * @return The player that is about to be removed.
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
