package fr.pederobien.mumble.server.interfaces;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import fr.pederobien.mumble.server.exceptions.ChannelAlreadyExistException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;

public interface IMumbleServer {

	/**
	 * @return The server name.
	 */
	String getName();

	/**
	 * Open this server.
	 */
	void open();

	/**
	 * Close this server.
	 */
	void close();

	/**
	 * @return If the server has been opened or the method {@link #close()} has not been called.
	 */
	boolean isOpened();

	/**
	 * Register a player to this server.
	 * 
	 * @param address    The address associated to this player
	 * @param playerName The name of the connected player.
	 * @param isAdmin    True if the player is an admin in game.
	 * 
	 * @return The registered player.
	 */
	IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin);

	/**
	 * Unregister a player from this server.
	 * 
	 * @param playerName The name of the disconnected player.
	 */
	void removePlayer(String playerName);

	/**
	 * Get a list that contains all players connected to the game. It does not means their mumble client is also connected.
	 * 
	 * @return A list that contains all registered players. This list is unmodifiable.
	 */
	List<IPlayer> getPlayers();

	/**
	 * Registered a channel to this server.
	 * 
	 * @param name              The channel's name.
	 * @param soundModifierName the sound modifier name attached to the channel to add.
	 * 
	 * @return The created channel.
	 * 
	 * @throws ChannelAlreadyExistException If there is already a channel registered for the given name.
	 */
	IChannel addChannel(String name, String soundModifierName);

	/**
	 * Unregistered a channel from this server.
	 * 
	 * @param name The channel's name.
	 * 
	 * @return The removed channel.
	 */
	IChannel removeChannel(String name);

	/**
	 * Rename the channel associated to the oldName.
	 * 
	 * @param oldName The old channel name.
	 * @param newName The new channel name.
	 * 
	 * @throws ChannelAlreadyExistException  If the channel associated to the new name already exists.
	 * @throws ChannelNotRegisteredException If the channel associated to the old name does not exists.
	 */
	void renameChannel(String oldName, String newName);

	/**
	 * @return An unmodifiable map that contains all registered channels for this mumble server.
	 */
	Map<String, IChannel> getChannels();

	/**
	 * Remove all registered channels from this server.
	 */
	List<IChannel> clearChannels();
}
