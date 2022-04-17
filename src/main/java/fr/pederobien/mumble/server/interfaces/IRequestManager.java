package fr.pederobien.mumble.server.interfaces;

import fr.pederobien.mumble.common.interfaces.IMumbleMessage;

public interface IRequestManager {

	/**
	 * Performs server configuration update according to the given request.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	IMumbleMessage answer(IMumbleMessage request);

	/**
	 * Send a message to the remote in order to add a channel to the server.
	 * 
	 * @param channel The added channel.
	 */
	void onChannelAdd(IChannel channel);

	/**
	 * Send a message to the remote in order to remove a channel from the server.
	 * 
	 * @param channel The removed channel.
	 */
	void onChannelRemove(IChannel channel);

	/**
	 * Send a message to the remote in order to update the channel name.
	 * 
	 * @param channel The channel whose the name has changed.
	 * @param oldName The old channel name.
	 */
	void onChannelNameChange(IChannel channel, String oldName);

	/**
	 * Send a message to the remote in order to register a new player.
	 * 
	 * @param player the added player.
	 */
	void onServerPlayerAdd(IPlayer player);

	/**
	 * Send a message to the remote in order to remove a player from the server.
	 * 
	 * @param name The name of the player to remove.
	 */
	void onServerPlayerRemove(String name);

	/**
	 * Send a message to the remote in order to rename a player.
	 * 
	 * @param oldName The name of the player to rename.
	 * @param newName The new player's name.
	 */
	void onPlayerNameChange(String oldName, String newName);

	/**
	 * Send a message to the remote in order to update the player online status.
	 * 
	 * @param player The player whose the online status has changed.
	 */
	void onPlayerOnlineChange(IPlayer player);

	/**
	 * Send a message to the remote in order to update the player game address.
	 * 
	 * @param player The player whose the game address has changed.
	 */
	void onPlayerGameAddressChange(IPlayer player);

	/**
	 * Send a message to the remote in order to update the player administrator status.
	 * 
	 * @param player The player whose the administrator status has changed.
	 */
	void onPlayerAdminChange(IPlayer player);

	/**
	 * Send a message to the remote in order to update the player mute status.
	 * 
	 * @param player The player whose the mute status has changed.
	 */
	void onPlayerMuteChange(IPlayer player);

	/**
	 * Send a message to the remote in order to mute or unmute a target player for a source player.
	 * 
	 * @param target The target player to mute or unmute for a source player.
	 * @param source The source player for which a target player is mute or unmute.
	 */
	void onPlayerMuteByChange(IPlayer target, IPlayer source);

	/**
	 * Send a message to the remote in order to update the player deafen status.
	 * 
	 * @param player The player whose the deafen status has changed.
	 */
	void onPlayerDeafenChange(IPlayer player);

	/**
	 * Send a message to the remote in order to kick a player from a channel.
	 * 
	 * @param kicked  The player that has been kicked by another player.
	 * @param kicking The player that has kicked another player.
	 */
	void onPlayerKick(IPlayer kicked, IPlayer kicking);

	/**
	 * Send a message to the remote in order to update the position of a player.
	 * 
	 * @param player The player whose the position has changed.
	 */
	void onPlayerPositionChange(IPlayer player);

	/**
	 * Send a message to the remote in order to add a player to a channel.
	 * 
	 * @param channel The channel to which a player has been added.
	 * @param player  The added player.
	 */
	void onChannelPlayerAdd(IChannel channel, IPlayer player);

	/**
	 * Send a message to the remote in order to remove a player from a channel.
	 * 
	 * @param channel The channel from which a player has been removed.
	 * @param player  The removed player.
	 */
	void onChannelPlayerRemove(IChannel channel, IPlayer player);

	/**
	 * Send a message to the remote in order to update the sound modifier associated to the given channel.
	 * 
	 * @param channel The channel whose the sound modifier has changed.
	 */
	void onSoundModifierChange(IChannel channel);

	/**
	 * Send a message to the remote in order to update the value of the given parameter.
	 * 
	 * @param parameter The parameter whose the value has changed.
	 */
	void onParameterValueChange(IParameter<?> parameter);

	/**
	 * Send a message to the remote in order to update the minimum value of the given parameter.
	 * 
	 * @param parameter The parameter whose the minimum value has changed.
	 */
	void onParameterMinValueChange(IRangeParameter<?> parameter);

	/**
	 * Send a message to the remote in order to update the maximum value of the given parameter.
	 * 
	 * @param parameter The parameter whose the maximum value has changed.
	 */
	void onParameterMaxValueChange(IRangeParameter<?> parameter);
}
