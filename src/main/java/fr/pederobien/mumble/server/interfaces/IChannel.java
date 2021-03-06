package fr.pederobien.mumble.server.interfaces;

import java.util.List;

import fr.pederobien.mumble.server.interfaces.observers.IObsChannel;
import fr.pederobien.persistence.interfaces.IUnmodifiableNominable;
import fr.pederobien.utils.IObservable;

public interface IChannel extends IUnmodifiableNominable, IObservable<IObsChannel> {

	/**
	 * Appends the given player to this channel. Once registered, the player can speak to players currently registered to this
	 * channel.
	 * 
	 * @param player The player to add.
	 */
	void addPlayer(IPlayer player);

	/**
	 * Removes the given player from this channel. Once unregistered, the player cannot speak to players currently registered to this
	 * channel.
	 * 
	 * @param player The player to remove.
	 */
	void removePlayer(IPlayer player);

	/**
	 * @return A list that contains all registered player to this channel. This list is unmodifiable.
	 */
	List<IPlayer> getPlayers();

	/**
	 * Removes all registered player from this channel.
	 */
	void clear();

	/**
	 * @return The sound modifier associated to this channel.
	 */
	ISoundModifier getSoundModifier();

	/**
	 * Set the sound modifier of this channel.
	 * 
	 * @param soundModifier The new volume modifier associated to this channel.
	 */
	void setSoundModifier(ISoundModifier soundModifier);
}
