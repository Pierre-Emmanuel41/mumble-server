package fr.pederobien.mumble.server.interfaces.observers;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public interface IObsChannel {

	/**
	 * Notify this observer the given channel has been renamed.
	 * 
	 * @param channel The renamed channel.
	 * @param oldName The old channel name
	 * @param newName The new channel name.
	 */
	void onChannelRenamed(IChannel channel, String oldName, String newName);

	/**
	 * Notify this observer the given player has been added to the specified channel.
	 * 
	 * @param channel The channel to which the player has been added.
	 * @param player  The added player.
	 */
	void onPlayerAdded(IChannel channel, IPlayer player);

	/**
	 * Notify this observer the given player has been removed from the specified channel.
	 * 
	 * @param channel The channel from which the player has been removed.
	 * @param player  The removed player.
	 */
	void onPlayerRemoved(IChannel channel, IPlayer player);

	/**
	 * Notify this observer the sound modifier of the specified channel has changed.
	 * 
	 * @param channel  The channel whose sound modifier has changed.
	 * @param modifier The new channel modifier.
	 */
	void onSoundModifierChanged(IChannel channel, ISoundModifier modifier);
}
