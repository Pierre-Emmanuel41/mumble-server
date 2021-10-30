package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IPlayer;

public class PlayerSpeakPostEvent extends PlayerEvent {
	private byte[] data;

	/**
	 * Creates an event thrown when a player speaks to other player.
	 * 
	 * @param player The speaking player.
	 * @param data   The bytes array that contains the audio sample.
	 */
	public PlayerSpeakPostEvent(IPlayer player, byte[] data) {
		super(player);
		this.data = data;
	}

	/**
	 * @return The bytes array that contains the audio sample.
	 */
	public byte[] getData() {
		return data;
	}
}
