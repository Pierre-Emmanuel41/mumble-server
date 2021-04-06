package fr.pederobien.mumble.server.interfaces;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import fr.pederobien.persistence.interfaces.IUnmodifiableNominable;

public interface IMumbleServer extends IUnmodifiableNominable {

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
	 * @param name The channel's name.
	 * 
	 * @return The created channel.
	 */
	IChannel addChannel(String name);

	/**
	 * Unregistered a channel from this server.
	 * 
	 * @param name The channel's name.
	 * 
	 * @return The removed channel.
	 */
	IChannel removeChannel(String name);

	/**
	 * @return An unmodifiable map that contains all registered channels for this mumble server.
	 */
	Map<String, IChannel> getChannels();
}
