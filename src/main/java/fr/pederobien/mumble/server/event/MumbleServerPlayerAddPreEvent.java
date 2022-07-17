package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;
import fr.pederobien.utils.ICancellable;

public class MumbleServerPlayerAddPreEvent extends MumbleServerPlayerListEvent implements ICancellable {
	private boolean isCancelled;
	private IPlayer player;

	/**
	 * Creates an event thrown when a player is about to be added on the server.
	 * 
	 * @param list   The list to which a player is about to be added.
	 * @param player The added player.
	 */
	public MumbleServerPlayerAddPreEvent(IServerPlayerList list, IPlayer player) {
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
	 * @return The player that is about to be added.
	 */
	public IPlayer getPlayer() {
		return player;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("list=" + getList().getName());
		joiner.add("name=" + getPlayer());
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
