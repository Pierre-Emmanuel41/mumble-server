package fr.pederobien.mumble.server.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.exceptions.ChannelAlreadyRegisteredException;
import fr.pederobien.mumble.server.exceptions.SoundModifierDoesNotExistException;

public interface IChannelList extends Iterable<IChannel> {

	/**
	 * @return The server associated to this channel list.
	 */
	IMumbleServer getServer();

	/**
	 * Appends the given channel to this list.
	 * 
	 * @param channelName       The channel name.
	 * @param soundModifierName the sound modifier name.
	 * 
	 * @return the created channel.
	 * 
	 * @throws ChannelAlreadyRegisteredException  If a channel is already registered for the channel name.
	 * @throws SoundModifierDoesNotExistException If the sound modifier name does not refer to registered sound modifier.
	 */
	IChannel add(String channelName, String soundModifierName);

	/**
	 * Removes the channel associated to the given name.
	 * 
	 * @param name The channel name to remove.
	 * 
	 * @return The removed channel if registered, null otherwise.
	 */
	IChannel remove(String name);

	/**
	 * Removes the given channel from this list.
	 * 
	 * @param channel The channel to remove.
	 * 
	 * @return True if the channel was registered, false otherwise.
	 */
	boolean remove(IChannel channel);

	/**
	 * Removes all registered channels. It also clear each registered players.
	 */
	void clear();

	/**
	 * Get the channel associated to the given name.
	 * 
	 * @param name The channel name.
	 * 
	 * @return An optional that contains the channel if registered, an empty optional otherwise.
	 */
	Optional<IChannel> getChannel(String name);

	/**
	 * @return a sequential {@code Stream} over the elements in this collection.
	 */
	Stream<IChannel> stream();

	/**
	 * @return A copy of the underlying list.
	 */
	List<IChannel> toList();

}
