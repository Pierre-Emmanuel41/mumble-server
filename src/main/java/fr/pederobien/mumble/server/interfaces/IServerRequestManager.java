package fr.pederobien.mumble.server.interfaces;

import fr.pederobien.mumble.common.interfaces.IMumbleMessage;

public interface IServerRequestManager {

	/**
	 * Performs server configuration update according to the given request.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	IMumbleMessage answer(IMumbleMessage request);

	/**
	 * Send a message to the remote in order to add a channel to the server.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param channel The added channel.
	 * 
	 * @return The message to send to the remote in order to add a channel.
	 */
	IMumbleMessage onChannelAdd(float version, IChannel channel);

	/**
	 * Send a message to the remote in order to remove a channel from the server.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param channel The removed channel.
	 * 
	 * @return The message to send to the remote in order to remove a channel.
	 */
	IMumbleMessage onChannelRemove(float version, IChannel channel);

	/**
	 * Send a message to the remote in order to update the channel name.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param channel The channel whose the name has changed.
	 * @param oldName The old channel name.
	 * 
	 * @return The message to send to the remote in order to rename a channel.
	 */
	IMumbleMessage onChannelNameChange(float version, IChannel channel, String oldName);

	/**
	 * Send a message to the remote in order to register a new player.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param player  the added player.
	 * 
	 * @return The message to send to the remote in order to add a player to a server.
	 */
	IMumbleMessage onServerPlayerAdd(float version, IPlayer player);

	/**
	 * Send a message to the remote in order to remove a player from the server.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param name    The name of the player to remove.
	 * 
	 * @return The message to send to the remote in order to remove a player from a server.
	 */
	IMumbleMessage onServerPlayerRemove(float version, String name);

	/**
	 * Send a message to the remote in order to rename a player.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param oldName The name of the player to rename.
	 * @param newName The new player's name.
	 * 
	 * @return The message to send to the remote in order to rename a player.
	 */
	IMumbleMessage onPlayerNameChange(float version, String oldName, String newName);

	/**
	 * Send a message to the remote in order to update the player online status.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param player  The player whose the online status has changed.
	 * 
	 * @return The message to send to the remote in order to update the online status of a player.
	 */
	IMumbleMessage onPlayerOnlineChange(float version, IPlayer player);

	/**
	 * Send a message to the remote in order to update the player game address.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param player  The player whose the game address has changed.
	 * 
	 * @return The message to send to the remote in order to update the game address of a player.
	 */
	IMumbleMessage onPlayerGameAddressChange(float version, IPlayer player);

	/**
	 * Send a message to the remote in order to update the player administrator status.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param player  The player whose the administrator status has changed.
	 * 
	 * @return The message to send to the remote in order to update the administrator status of a player.
	 */
	IMumbleMessage onPlayerAdminChange(float version, IPlayer player);

	/**
	 * Send a message to the remote in order to update the player mute status.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param player  The player whose the mute status has changed.
	 * 
	 * @return The message to send to the remote in order to update the mute status of a player.
	 */
	IMumbleMessage onPlayerMuteChange(float version, IPlayer player);

	/**
	 * Send a message to the remote in order to mute or unmute a target player for a source player.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param target  The target player to mute or unmute for a source player.
	 * @param source  The source player for which a target player is mute or unmute.
	 * 
	 * @return The message to send to the remote in order to update the muteby status of a player.
	 */
	IMumbleMessage onPlayerMuteByChange(float version, IPlayer target, IPlayer source);

	/**
	 * Send a message to the remote in order to update the player deafen status.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param player  The player whose the deafen status has changed.
	 * 
	 * @return The message to send to the remote in order to update the deafen status of a player.
	 */
	IMumbleMessage onPlayerDeafenChange(float version, IPlayer player);

	/**
	 * Send a message to the remote in order to kick a player from a channel.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param kicked  The player that has been kicked by another player.
	 * @param kicking The player that has kicked another player.
	 * 
	 * @return The message to send to the remote in order to kick a player from a channel.
	 */
	IMumbleMessage onPlayerKick(float version, IPlayer kicked, IPlayer kicking);

	/**
	 * Send a message to the remote in order to update the position of a player.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param player  The player whose the position has changed.
	 * 
	 * @return The message to send to the remote in order to update the position of a player.
	 */
	IMumbleMessage onPlayerPositionChange(float version, IPlayer player);

	/**
	 * Send a message to the remote in order to add a player to a channel.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param channel The channel to which a player has been added.
	 * @param player  The added player.
	 * 
	 * @return The message to send to the remote in order to add a player in a channel.
	 */
	IMumbleMessage onChannelPlayerAdd(float version, IChannel channel, IPlayer player);

	/**
	 * Send a message to the remote in order to remove a player from a channel.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param channel The channel from which a player has been removed.
	 * @param player  The removed player.
	 * 
	 * @return The message to send to the remote in order to remove a player from a channel
	 */
	IMumbleMessage onChannelPlayerRemove(float version, IChannel channel, IPlayer player);

	/**
	 * Send a message to the remote in order to update the value of the given parameter.
	 * 
	 * @param version   The protocol version to use to create a mumble message.
	 * @param parameter The parameter whose the value has changed.
	 * 
	 * @return The message to send to the remote in order to update the value of a parameter.
	 */
	IMumbleMessage onParameterValueChange(float version, IParameter<?> parameter);

	/**
	 * Send a message to the remote in order to update the minimum value of the given parameter.
	 * 
	 * @param version   The protocol version to use to create a mumble message.
	 * @param parameter The parameter whose the minimum value has changed.
	 * 
	 * @return The message to send to the remote in order to update the minimum value of a parameter.
	 */
	IMumbleMessage onParameterMinValueChange(float version, IRangeParameter<?> parameter);

	/**
	 * Send a message to the remote in order to update the maximum value of the given parameter.
	 * 
	 * @param version   The protocol version to use to create a mumble message.
	 * @param parameter The parameter whose the maximum value has changed.
	 * 
	 * @return The message to send to the remote in order to update the maximum value of a parameter.
	 */
	IMumbleMessage onParameterMaxValueChange(float version, IRangeParameter<?> parameter);

	/**
	 * Send a message to the remote in order to update the sound modifier associated to the given channel.
	 * 
	 * @param version The protocol version to use to create a mumble message.
	 * @param channel The channel whose the sound modifier has changed.
	 * 
	 * @return The message to send to the remote in order to set the sound modifier of a channel.
	 */
	IMumbleMessage onChannelSoundModifierChange(float version, IChannel channel);
}
