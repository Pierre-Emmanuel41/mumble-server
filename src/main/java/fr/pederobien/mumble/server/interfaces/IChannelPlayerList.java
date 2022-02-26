package fr.pederobien.mumble.server.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.exceptions.PlayerAlreadyRegisteredException;

public interface IChannelPlayerList extends Iterable<IPlayer> {

	/**
	 * @return The channel to which this list is associated.
	 */
	IChannel getChannel();

	/**
	 * @return The name of this player list.
	 */
	String getName();

	/**
	 * Appends the given player to this list.
	 * 
	 * @param player The player to add.
	 * 
	 * @throws PlayerAlreadyRegisteredException If a player is already registered for the player name.
	 */
	void add(IPlayer player);

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
}
