package fr.pederobien.mumble.server.interfaces;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface IPlayerList extends Iterable<IPlayer> {

	/**
	 * @return The name of this list.
	 */
	String getName();

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
	 * Removes the player associated to the given name.
	 * 
	 * @param name The player name to remove.
	 * 
	 * @return The removed player if registered, null otherwise.
	 */
	IPlayer remove(String name);

	/**
	 * Removes the given player from this list.
	 * 
	 * @param player The player to remove.
	 * 
	 * @return True if the player was registered, false otherwise.
	 */
	boolean remove(IPlayer player);

	/**
	 * Get the player associated to the given name.
	 * 
	 * @param name The player name.
	 * 
	 * @return An optional that contains the player if registered, an empty optional otherwise.
	 */
	Optional<IPlayer> getPlayer(String name);

	/**
	 * @return A list that contains players registered in channels.
	 */
	List<IPlayer> getPlayersInChannel();

	/**
	 * @return a sequential {@code Stream} over the elements in this collection.
	 */
	Stream<IPlayer> stream();

	/**
	 * @return A copy of the underlying list.
	 */
	List<IPlayer> toList();
}
