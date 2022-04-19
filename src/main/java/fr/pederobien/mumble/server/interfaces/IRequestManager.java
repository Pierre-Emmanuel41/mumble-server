package fr.pederobien.mumble.server.interfaces;

import fr.pederobien.mumble.common.interfaces.IMumbleMessage;

public interface IRequestManager {

	/**
	 * @return The version of the communication protocol associated to this requests manager.
	 */
	float getVersion();

	/**
	 * Performs server configuration update according to the given request.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	IMumbleMessage answer(IMumbleMessage request);

	/**
	 * @return The message to send to the remote in order to get the latest version of the communication protocol.
	 */
	IMumbleMessage getCommunicationProtocolVersion();

	/**
	 * Creates a message in order to set a specific version of the communication protocol.
	 * 
	 * @param version The version to use between the client and the remote.
	 * 
	 * @return The message to send to the remote in order to get the latest version of the communication protocol.
	 */
	IMumbleMessage setCommunicationProtocolVersion(float version);

	/**
	 * Creates a message in order to add a channel to the server.
	 * 
	 * @param channel The added channel.
	 * 
	 * @return The message to send to the remote in order to add a channel.
	 */
	IMumbleMessage onChannelAdd(IChannel channel);

	/**
	 * Creates a message in order to remove a channel from the server.
	 * 
	 * @param channel The removed channel.
	 * 
	 * @return The message to send to the remote in order to remove a channel.
	 */
	IMumbleMessage onChannelRemove(IChannel channel);

	/**
	 * Creates a message in order to update the channel name.
	 * 
	 * @param channel The channel whose the name has changed.
	 * @param oldName The old channel name.
	 * 
	 * @return The message to send to the remote in order to rename a channel.
	 */
	IMumbleMessage onChannelNameChange(IChannel channel, String oldName);

	/**
	 * Creates a message in order to register a new player.
	 * 
	 * @param player the added player.
	 * 
	 * @return The message to send to the remote in order to add a player to a server.
	 */
	IMumbleMessage onServerPlayerAdd(IPlayer player);

	/**
	 * Creates a message in order to remove a player from the server.
	 * 
	 * @param name The name of the player to remove.
	 * 
	 * @return The message to send to the remote in order to remove a player from a server.
	 */
	IMumbleMessage onServerPlayerRemove(String name);

	/**
	 * Creates a message in order to rename a player.
	 * 
	 * @param oldName The name of the player to rename.
	 * @param newName The new player's name.
	 * 
	 * @return The message to send to the remote in order to rename a player.
	 */
	IMumbleMessage onPlayerNameChange(String oldName, String newName);

	/**
	 * Creates a message in order to update the player online status.
	 * 
	 * @param player The player whose the online status has changed.
	 * 
	 * @return The message to send to the remote in order to update the online status of a player.
	 */
	IMumbleMessage onPlayerOnlineChange(IPlayer player);

	/**
	 * Creates a message in order to update the player game address.
	 * 
	 * @param player The player whose the game address has changed.
	 * 
	 * @return The message to send to the remote in order to update the game address of a player.
	 */
	IMumbleMessage onPlayerGameAddressChange(IPlayer player);

	/**
	 * Creates a message in order to update the player administrator status.
	 * 
	 * @param player The player whose the administrator status has changed.
	 * 
	 * @return The message to send to the remote in order to update the administrator status of a player.
	 */
	IMumbleMessage onPlayerAdminChange(IPlayer player);

	/**
	 * Creates a message in order to update the player mute status.
	 * 
	 * @param player The player whose the mute status has changed.
	 * 
	 * @return The message to send to the remote in order to update the mute status of a player.
	 */
	IMumbleMessage onPlayerMuteChange(IPlayer player);

	/**
	 * Creates a message in order to mute or unmute a target player for a source player.
	 * 
	 * @param target The target player to mute or unmute for a source player.
	 * @param source The source player for which a target player is mute or unmute.
	 * 
	 * @return The message to send to the remote in order to update the muteby status of a player.
	 */
	IMumbleMessage onPlayerMuteByChange(IPlayer target, IPlayer source);

	/**
	 * Creates a message in order to update the player deafen status.
	 * 
	 * @param player The player whose the deafen status has changed.
	 * 
	 * @return The message to send to the remote in order to update the deafen status of a player.
	 */
	IMumbleMessage onPlayerDeafenChange(IPlayer player);

	/**
	 * Creates a message in order to kick a player from a channel.
	 * 
	 * @param kicked  The player that has been kicked by another player.
	 * @param kicking The player that has kicked another player.
	 * 
	 * @return The message to send to the remote in order to kick a player from a channel.
	 */
	IMumbleMessage onPlayerKick(IPlayer kicked, IPlayer kicking);

	/**
	 * Creates a message in order to update the position of a player.
	 * 
	 * @param player The player whose the position has changed.
	 * 
	 * @return The message to send to the remote in order to update the position of a player.
	 */
	IMumbleMessage onPlayerPositionChange(IPlayer player);

	/**
	 * Creates a message in order to add a player to a channel.
	 * 
	 * @param channel The channel to which a player has been added.
	 * @param player  The added player.
	 * 
	 * @return The message to send to the remote in order to add a player in a channel.
	 */
	IMumbleMessage onChannelPlayerAdd(IChannel channel, IPlayer player);

	/**
	 * Creates a message in order to remove a player from a channel.
	 * 
	 * @param channel The channel from which a player has been removed.
	 * @param player  The removed player.
	 * 
	 * @return The message to send to the remote in order to remove a player from a channel
	 */
	IMumbleMessage onChannelPlayerRemove(IChannel channel, IPlayer player);

	/**
	 * Creates a message in order to update the value of the given parameter.
	 * 
	 * @param parameter The parameter whose the value has changed.
	 * 
	 * @return The message to send to the remote in order to update the value of a parameter.
	 */
	IMumbleMessage onParameterValueChange(IParameter<?> parameter);

	/**
	 * Creates a message in order to update the minimum value of the given parameter.
	 * 
	 * @param parameter The parameter whose the minimum value has changed.
	 * 
	 * @return The message to send to the remote in order to update the minimum value of a parameter.
	 */
	IMumbleMessage onParameterMinValueChange(IRangeParameter<?> parameter);

	/**
	 * Creates a message in order to update the maximum value of the given parameter.
	 * 
	 * @param parameter The parameter whose the maximum value has changed.
	 * 
	 * @return The message to send to the remote in order to update the maximum value of a parameter.
	 */
	IMumbleMessage onParameterMaxValueChange(IRangeParameter<?> parameter);

	/**
	 * Creates a message in order to update the sound modifier associated to the given channel.
	 * 
	 * @param channel The channel whose the sound modifier has changed.
	 * 
	 * @return The message to send to the remote in order to set the sound modifier of a channel.
	 */
	IMumbleMessage onChannelSoundModifierChange(IChannel channel);
}
