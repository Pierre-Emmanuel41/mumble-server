package fr.pederobien.mumble.server.interfaces;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.exceptions.PlayerAlreadyRegisteredException;

public interface IServerPlayerList extends Iterable<IPlayer> {

	/**
	 * @return The name of this player list.
	 */
	String getName();

	/**
	 * Creates a player and register it.
	 * 
	 * @param name        The player's name.
	 * @param gameAddress The player's address to play to the game.
	 * @param isAdmin     The player's administrator status.
	 * @param x           The player's x coordinate.
	 * @param x           The player's y coordinate.
	 * @param x           The player's z coordinate.
	 * @param x           The player's yaw angle.
	 * @param x           The player's pitch angle.
	 * 
	 * @return The created player.
	 * 
	 * @throws PlayerAlreadyRegisteredException If a player is already registered for the player name.
	 */
	IPlayer add(String name, InetSocketAddress gameAddress, boolean isAdmin, double x, double y, double z, double yaw, double pitch);

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
	 * Removes all registered players. It also clear each registered players.
	 */
	void clear();

	/**
	 * Get the player associated to the given name.
	 * 
	 * @param name The player name.
	 * 
	 * @return An optional that contains the player if registered, an empty optional otherwise.
	 */
	Optional<IPlayer> getPlayer(String name);

	/**
	 * @return a sequential {@code Stream} over the elements in this collection.
	 */
	Stream<IPlayer> stream();

	/**
	 * @return A copy of the underlying list.
	 */
	List<IPlayer> toList();

	/**
	 * @return A list that contains players registered in channels.
	 */
	List<IPlayer> getPlayersInChannel();
}
