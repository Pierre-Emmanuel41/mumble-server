package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IPosition;

public class PlayerPositionChangeEvent extends PlayerEvent {
	private IPosition position;

	/**
	 * Creates an event thrown when the position of a player has changed.
	 * 
	 * @param position The new player position.
	 */
	public PlayerPositionChangeEvent(IPosition position) {
		super(position.getPlayer());
		this.position = position;
	}

	/**
	 * @return The new player position.
	 */
	public IPosition getPosition() {
		return position;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("player=" + getPlayer().getName());
		joiner.add("position=" + getPosition());
		return String.format("%s_%s", getName(), joiner);
	}
}
