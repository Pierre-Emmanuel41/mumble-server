package fr.pederobien.mumble.server.interfaces;

import java.net.InetSocketAddress;
import java.util.UUID;

import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;

public interface IPlayer {

	/**
	 * @return The player name.
	 */
	String getName();

	/**
	 * Set the name of this player.
	 * 
	 * @param name The new player name.
	 */
	void setName(String name);

	/**
	 * @return The address used by the player to play to the game.
	 */
	InetSocketAddress getGameAddress();

	/**
	 * @return The position in game of this player.
	 */
	IPosition getPosition();

	/**
	 * @return True if this player is an admin for this server.
	 */
	boolean isAdmin();

	/**
	 * Set if the player is admin or not.
	 * 
	 * @param isAdmin True if the player is admin, false otherwise.
	 */
	void setAdmin(boolean isAdmin);

	/**
	 * @return True if the player is connected in game, false otherwise;
	 */
	boolean isOnline();

	/**
	 * Set the online status of a player.
	 * 
	 * @param isOnline The new player's online status.
	 */
	void setOnline(boolean isOnline);

	/**
	 * @return The unique identifier associated to this player.
	 */
	UUID getUUID();

	/**
	 * @return The channel in which this player is registered or null if it is not registered in any channels.
	 */
	IChannel getChannel();

	/**
	 * @return True if this player is mute, false otherwise.
	 */
	boolean isMute();

	/**
	 * Set the new mute status of the player.
	 * 
	 * @param isMute True if the player is mute, false otherwise.
	 * 
	 * @throws PlayerNotRegisteredInChannelException If this player is not registered in a channel.
	 */
	void setMute(boolean isMute);

	/**
	 * @return True if this player is deafen, false otherwise.
	 */
	boolean isDeafen();

	/**
	 * Set the new deafen status of the player.
	 * 
	 * @param isDeafen True if the player is deafen, false otherwise.
	 * 
	 * @throws PlayerNotRegisteredInChannelException If this player is not registered in a channel.
	 */
	void setDeafen(boolean isDeafen);
}
