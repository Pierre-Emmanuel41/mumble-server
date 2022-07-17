package fr.pederobien.mumble.server.event;

import java.net.InetSocketAddress;
import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumblePlayerGameAddressChangePostEvent extends MumblePlayerEvent {
	private InetSocketAddress oldGameAddress;

	/**
	 * Creates an event thrown when the game address of a player has changed.
	 * 
	 * @param player         The player whose the game address has changed.
	 * @param oldGameAddress The old player's game address.
	 */
	public MumblePlayerGameAddressChangePostEvent(IPlayer player, InetSocketAddress oldGameAddress) {
		super(player);
		this.oldGameAddress = oldGameAddress;
	}

	/**
	 * @return The old player's game address.
	 */
	public InetSocketAddress getOldGameAddress() {
		return oldGameAddress;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("currentGameAddress=" + getPlayer().getGameAddress());
		joiner.add("oldGameAddress=" + getOldGameAddress());
		return String.format("%s_%s", getName(), joiner);
	}
}
