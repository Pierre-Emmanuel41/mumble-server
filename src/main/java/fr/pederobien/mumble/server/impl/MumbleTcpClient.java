package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumbleTcpClient {
	private ITcpConnection connection;

	/**
	 * Creates a Mumble TCP client associated to the given connection.
	 * 
	 * @param connection The connection with the remote.
	 */
	public MumbleTcpClient(ITcpConnection connection) {
		this.connection = connection;
	}

	/**
	 * @return The connection with the remote.
	 */
	public ITcpConnection getConnection() {
		return connection;
	}

	/**
	 * Send a message to the remote in order to update the player status admin.
	 * 
	 * @param player The player whose the admin status has changed.
	 */
	public void onPlayerAdminChange(IPlayer player) {
		send(Idc.PLAYER_ADMIN, Oid.SET, player.getName(), player.isAdmin());
	}

	/**
	 * Send a message to the remote in order to update the player online status.
	 * 
	 * @param player The player whose the online status has changed.
	 */
	public void onPlayerOnlineChange(IPlayer player) {
		if (player.isOnline()) {
			List<Object> properties = new ArrayList<Object>();
			// Player's name
			properties.add(player.getName());

			// Player's online status
			properties.add(player.isOnline());

			// Player's IP address
			properties.add(player.getGameAddress().getAddress().getAddress());

			// Player's game port number
			properties.add(player.getGameAddress().getPort());

			// Player's administrator status
			properties.add(player.isAdmin());

			// Player's identifier
			properties.add(player.getUUID());

			send(Idc.PLAYER, Oid.SET, properties.toArray());
		} else {
			if (player.getChannel() != null)
				player.getChannel().getPlayers().remove(player);
			send(Idc.PLAYER, Oid.SET, player.getName(), false);
		}
	}

	/**
	 * Send a message to the remote in order to update the player mute status.
	 * 
	 * @param player The player whose the mute status has changed.
	 */
	public void onPlayerMuteChange(IPlayer player) {
		send(Idc.PLAYER_MUTE, Oid.SET, player.getName(), player.isMute());
	}

	/**
	 * Send a message to the remote in order to update the player deafen status.
	 * 
	 * @param player The player whose the deafen status has changed.
	 */
	public void onPlayerDeafenChange(IPlayer player) {
		send(Idc.PLAYER_DEAFEN, Oid.SET, player.getName(), player.isDeafen());
	}

	/**
	 * Send a message to the remote in order to add a channel to the server.
	 * 
	 * @param channel The added channel.
	 */
	public void onChannelAdd(IChannel channel) {
		List<Object> informations = new ArrayList<Object>();

		// Channel's name
		informations.add(channel.getName());

		// Modifier's name
		informations.add(channel.getSoundModifier().getName());

		// Number of parameters
		informations.add(channel.getSoundModifier().getParameters().size());

		for (IParameter<?> parameter : channel.getSoundModifier().getParameters()) {
			// Parameter's name
			informations.add(parameter.getName());

			// Parameter's type
			informations.add(parameter.getType());

			// Parameter's value
			informations.add(parameter.getValue());
		}

		send(Idc.CHANNELS, Oid.ADD, informations.toArray());
	}

	/**
	 * Send a message to the remote in order to remove a channel from the server.
	 * 
	 * @param channel The removed channel.
	 */
	public void onChannelRemove(IChannel channel) {
		send(Idc.CHANNELS, Oid.REMOVE, channel.getName());
	}

	/**
	 * Send e message to the remote in order to update the channel name.
	 * 
	 * @param channel The channel whose the name has changed.
	 * @param oldName The old channel name.
	 */
	public void onChannelNameChange(IChannel channel, String oldName) {
		send(Idc.CHANNELS, Oid.SET, oldName, channel.getName());
	}

	/**
	 * Send a message to the remote in order to add a player to a channel.
	 * 
	 * @param channel The channel to which a player has been added.
	 * @param player  The added player.
	 */
	public void onPlayerAdd(IChannel channel, IPlayer player) {
		send(Idc.CHANNELS_PLAYER, Oid.ADD, channel.getName(), player.getName());
	}

	/**
	 * Send a message to the remote in order to remove a player from a channel.
	 * 
	 * @param channel The channel from which a player has been removed.
	 * @param player  The removed player.
	 */
	public void onPlayerRemove(IChannel channel, IPlayer player) {
		send(Idc.CHANNELS_PLAYER, Oid.REMOVE, channel.getName(), player.getName());
	}

	/**
	 * Send a message to the remote in order to update the sound modifier associated to the given channel.
	 * 
	 * @param channel The channel whose the sound modifier has changed.
	 */
	public void onSoundModifierChange(IChannel channel) {
		List<Object> informations = new ArrayList<Object>();

		// Channel's name
		informations.add(channel.getName());

		// Modifier's name
		informations.add(channel.getSoundModifier().getName());

		// Number of parameters
		informations.add(channel.getSoundModifier().getParameters().size());
		for (IParameter<?> parameter : channel.getSoundModifier().getParameters()) {
			// Parmaeter's name
			informations.add(parameter.getName());

			// Parameter's type
			informations.add(parameter.getType());

			// Parameter's value
			informations.add(parameter.getValue());
		}

		send(Idc.SOUND_MODIFIER, Oid.SET, informations.toArray());
	}

	/**
	 * Send a message to the remote in order to update the value of the given parameter.
	 * 
	 * @param <T>       The underlying type of the parameter.
	 * @param parameter The parameter whose the value has changed.
	 */
	public <T> void onParameterValueChange(IParameter<T> parameter) {
		List<Object> informations = new ArrayList<Object>();

		// Channel's name
		informations.add(parameter.getSoundModifier().getChannel().getName());

		// Modifier's name
		informations.add(parameter.getSoundModifier().getName());

		// Number of parameters
		informations.add(1);

		// Parameter's name
		informations.add(parameter.getName());

		// Parameter's type
		informations.add(parameter.getType());

		// Parameter's value
		informations.add(parameter.getValue());

		send(Idc.SOUND_MODIFIER, Oid.SET, informations.toArray());
	}

	/**
	 * Send the given message to the remote.
	 * 
	 * @param message The message to send.
	 */
	public void send(IMumbleMessage message) {
		if (connection == null || connection.isDisposed())
			return;
		connection.send(new MumbleCallbackMessage(message, null));
	}

	/**
	 * Send a message based on the given parameter to the remote.
	 * 
	 * @param idc       The message idc.
	 * @param oid       The message oid.
	 * @param errorCode The message errorCode.
	 * @param payload   The message payload.
	 */
	public void send(Idc idc, Oid oid, ErrorCode errorCode, Object... payload) {
		send(MumbleServerMessageFactory.create(idc, oid, errorCode, payload));
	}

	/**
	 * Send a message based on the given parameter to the remote.
	 * 
	 * @param idc     The message idc.
	 * @param oid     The message oid.
	 * @param payload The message payload.
	 */
	public void send(Idc idc, Oid oid, Object... payload) {
		send(idc, oid, ErrorCode.NONE, payload);
	}

	/**
	 * Send a message based on the given parameter to the remote.
	 * 
	 * @param idc     The message idc.
	 * @param payload The message payload.
	 */
	public void send(Idc idc, Object... payload) {
		send(idc, Oid.GET, payload);
	}
}