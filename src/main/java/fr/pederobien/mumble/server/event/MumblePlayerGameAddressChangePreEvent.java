package fr.pederobien.mumble.server.event;

import java.net.InetSocketAddress;
import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.ICancellable;

public class MumblePlayerGameAddressChangePreEvent extends MumblePlayerEvent implements ICancellable {
	private boolean isCancelled;
	private InetSocketAddress newGameAddress;

	/**
	 * Creates an event thrown when the game address of a player is about to change.
	 * 
	 * @param player         The player whose the game address is about to change.
	 * @param newGameAddress The new player's game address.
	 */
	public MumblePlayerGameAddressChangePreEvent(IPlayer player, InetSocketAddress newGameAddress) {
		super(player);
		this.newGameAddress = newGameAddress;
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
	 * @return The new player's game address.
	 */
	public InetSocketAddress getNewGameAddress() {
		return newGameAddress;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("currentGameAddress=" + getPlayer().getGameAddress());
		joiner.add("newGameAddress=" + getNewGameAddress());
		return String.format("%s_%s", getName(), joiner);
	}
}
