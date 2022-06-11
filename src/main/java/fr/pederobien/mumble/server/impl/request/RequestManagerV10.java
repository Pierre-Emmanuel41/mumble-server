package fr.pederobien.mumble.server.impl.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Identifier;
import fr.pederobien.mumble.common.impl.messages.v10.AddPlayerToChannelV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetChannelInfoV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetChannelSoundModifierV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetChannelsInfoV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetFullServerConfigurationV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetParameterMaxValueV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetParameterMinValueV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetParameterValueV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetPlayerAdministratorStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetPlayerDeafenStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetPlayerGameAddressV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetPlayerMuteStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetPlayerOnlineStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetPlayerPositionV10;
import fr.pederobien.mumble.common.impl.messages.v10.GetSoundModifiersInfoV10;
import fr.pederobien.mumble.common.impl.messages.v10.KickPlayerFromChannelV10;
import fr.pederobien.mumble.common.impl.messages.v10.RegisterChannelOnServerV10;
import fr.pederobien.mumble.common.impl.messages.v10.RegisterPlayerOnServerV10;
import fr.pederobien.mumble.common.impl.messages.v10.RemovePlayerFromChannelV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetChannelNameV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetChannelSoundModifierV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetParameterMaxValueV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetParameterMinValueV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetParameterValueV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerAdministratorStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerDeafenStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerGameAddressV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerMuteByStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerMuteStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerNameV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerOnlineStatusV10;
import fr.pederobien.mumble.common.impl.messages.v10.SetPlayerPositionV10;
import fr.pederobien.mumble.common.impl.messages.v10.UnregisterChannelFromServerV10;
import fr.pederobien.mumble.common.impl.messages.v10.UnregisterPlayerFromServerV10;
import fr.pederobien.mumble.common.impl.messages.v10.model.ParameterInfo.FullParameterInfo;
import fr.pederobien.mumble.common.impl.messages.v10.model.PlayerInfo.FullPlayerInfo;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.exceptions.PlayerMumbleClientNotJoinedException;
import fr.pederobien.mumble.server.exceptions.PlayerNotAdministratorException;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.impl.PlayerMumbleClient;
import fr.pederobien.mumble.server.impl.RequestReceivedHolder;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.impl.modifiers.ParameterList;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;
import fr.pederobien.mumble.server.interfaces.IRangeParameter;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class RequestManagerV10 extends RequestManager {

	/**
	 * Creates a request management in order to modify the given getServer() and answer to remote getRequests().
	 * 
	 * @param server The server to update.
	 */
	public RequestManagerV10(IMumbleServer server) {
		super(server, 1.0f);

		// Server messages
		getRequests().put(Identifier.GET_FULL_SERVER_CONFIGURATION, holder -> getFullServerConfiguration((GetFullServerConfigurationV10) holder.getRequest()));
		getRequests().put(Identifier.GET_SERVER_CONFIGURATION, holder -> getServerConfiguration(holder));

		// Player messages
		getRequests().put(Identifier.GET_PLAYER_INFO, holder -> getPlayerInfo(holder));
		getRequests().put(Identifier.REGISTER_PLAYER_ON_SERVER, holder -> registerPlayerOnServer((RegisterPlayerOnServerV10) holder.getRequest()));
		getRequests().put(Identifier.UNREGISTER_PLAYER_FROM_SERVER, holder -> unregisterPlayerOnServer((UnregisterPlayerFromServerV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PLAYER_ONLINE_STATUS, holder -> getPlayerOnlineStatus((GetPlayerOnlineStatusV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PLAYER_ONLINE_STATUS, holder -> setPlayerOnlineStatus((SetPlayerOnlineStatusV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PLAYER_NAME, holder -> renamePlayer((SetPlayerNameV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PLAYER_GAME_ADDRESS, holder -> getPlayerGameAddress((GetPlayerGameAddressV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PLAYER_GAME_ADDRESS, holder -> setPlayerGameAddress((SetPlayerGameAddressV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PLAYER_ADMINISTRATOR, holder -> getPlayerAdmin((GetPlayerAdministratorStatusV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PLAYER_ADMINISTRATOR, holder -> setPlayerAdmin((SetPlayerAdministratorStatusV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PLAYER_MUTE, holder -> getPlayerMute((GetPlayerMuteStatusV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PLAYER_MUTE, holder -> setPlayerMute(holder));
		getRequests().put(Identifier.SET_PLAYER_MUTE_BY, holder -> setPlayerMuteBy((SetPlayerMuteByStatusV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PLAYER_DEAFEN, holder -> getPlayerDeafen((GetPlayerDeafenStatusV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PLAYER_DEAFEN, holder -> setPlayerDeafen((SetPlayerDeafenStatusV10) holder.getRequest()));
		getRequests().put(Identifier.KICK_PLAYER_FROM_CHANNEL, holder -> kickPlayerFromChannel((KickPlayerFromChannelV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PLAYER_POSITION, holder -> getPlayerPosition((GetPlayerPositionV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PLAYER_POSITION, holder -> setPlayerPosition((SetPlayerPositionV10) holder.getRequest()));

		// Channel messages
		getRequests().put(Identifier.GET_CHANNELS_INFO, holder -> getChannelsInfo((GetChannelsInfoV10) holder.getRequest()));
		getRequests().put(Identifier.GET_CHANNEL_INFO, holder -> getChannelInfo((GetChannelInfoV10) holder.getRequest()));
		getRequests().put(Identifier.REGISTER_CHANNEL_ON_THE_SERVER, holder -> registerChannelOnServer((RegisterChannelOnServerV10) holder.getRequest()));
		getRequests().put(Identifier.UNREGISTER_CHANNEL_FROM_SERVER, holder -> unregisterChannelFromServer((UnregisterChannelFromServerV10) holder.getRequest()));
		getRequests().put(Identifier.SET_CHANNEL_NAME, holder -> renameChannel((SetChannelNameV10) holder.getRequest()));
		getRequests().put(Identifier.ADD_PLAYER_TO_CHANNEL, holder -> addPlayerToChannel(holder));
		getRequests().put(Identifier.REMOVE_PLAYER_FROM_CHANNEL, holder -> removePlayerFromChannel(holder));

		// Parameter message
		getRequests().put(Identifier.GET_PARAMETER_VALUE, holder -> getParameterValue((GetParameterValueV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PARAMETER_VALUE, holder -> setParameterValue((SetParameterValueV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PARAMETER_MIN_VALUE, holder -> getParameterMinValue((GetParameterMinValueV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PARAMETER_MIN_VALUE, holder -> setParameterMinValue((SetParameterMinValueV10) holder.getRequest()));
		getRequests().put(Identifier.GET_PARAMETER_MAX_VALUE, holder -> getParameterMaxValue((GetParameterMaxValueV10) holder.getRequest()));
		getRequests().put(Identifier.SET_PARAMETER_MAX_VALUE, holder -> setParameterMaxValue((SetParameterMaxValueV10) holder.getRequest()));

		// Sound modifier messages
		getRequests().put(Identifier.GET_SOUND_MODIFIERS_INFO, holder -> getSoundModifiersInfo((GetSoundModifiersInfoV10) holder.getRequest()));
		getRequests().put(Identifier.GET_CHANNEL_SOUND_MODIFIER_INFO, holder -> getChannelSoundModifier((GetChannelSoundModifierV10) holder.getRequest()));
		getRequests().put(Identifier.SET_CHANNEL_SOUND_MODIFIER, holder -> setChannelSoundModifier((SetChannelSoundModifierV10) holder.getRequest()));
	}

	@Override
	public IMumbleMessage getCommunicationProtocolVersion() {
		return create(getVersion(), Identifier.GET_CP_VERSIONS);
	}

	@Override
	public IMumbleMessage setCommunicationProtocolVersion(float version) {
		return create(getVersion(), Identifier.SET_CP_VERSION, version);
	}

	@Override
	public IMumbleMessage onChannelAdd(IChannel channel) {
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

			// Parameter's value
			informations.add(parameter.getDefaultValue());

			// Parameter's range
			boolean isRange = parameter instanceof IRangeParameter<?>;
			informations.add(isRange);

			if (isRange) {
				IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;

				// Parameter's minimum value
				informations.add(rangeParameter.getMin());

				// Parameter's maximum value
				informations.add(rangeParameter.getMax());
			}
		}

		return create(getVersion(), Identifier.REGISTER_CHANNEL_ON_THE_SERVER, informations.toArray());
	}

	@Override
	public IMumbleMessage onChannelRemove(IChannel channel) {
		return create(getVersion(), Identifier.UNREGISTER_CHANNEL_FROM_SERVER, channel.getName());
	}

	@Override
	public IMumbleMessage onChannelNameChange(IChannel channel, String oldName) {
		return create(getVersion(), Identifier.SET_CHANNEL_NAME, oldName, channel.getName());
	}

	@Override
	public IMumbleMessage onServerPlayerAdd(IPlayer player) {
		List<Object> properties = new ArrayList<Object>();

		// Player's name
		properties.add(player.getName());

		// Player's game address
		properties.add(player.getGameAddress().getAddress().getHostAddress());

		// Player's gamePort
		properties.add(player.getGameAddress().getPort());

		// Player's identifier
		properties.add(player.getIdentifier());

		// Player's administrator status
		properties.add(player.isAdmin());

		// Player's mute status
		properties.add(player.isMute());

		// Player's deafen status
		properties.add(player.isDeafen());

		// Player's x coordinate
		properties.add(player.getPosition().getX());

		// Player's y coordinate
		properties.add(player.getPosition().getY());

		// Player's z coordinate
		properties.add(player.getPosition().getZ());

		// Player's yaw angle
		properties.add(player.getPosition().getYaw());

		// Player's pitch
		properties.add(player.getPosition().getPitch());

		return create(getVersion(), Identifier.REGISTER_PLAYER_ON_SERVER, properties.toArray());
	}

	@Override
	public IMumbleMessage onServerPlayerRemove(String name) {
		return create(getVersion(), Identifier.UNREGISTER_PLAYER_FROM_SERVER, name);
	}

	@Override
	public IMumbleMessage onPlayerInfoChanged(IPlayer player) {
		List<Object> properties = new ArrayList<Object>();

		boolean isOnline = player == null ? false : player.isOnline();

		// Player's online status
		properties.add(isOnline);

		if (isOnline) {

			// Player's name
			properties.add(player.getName());

			// Player's identifier
			properties.add(player.getIdentifier());

			// Player's game address
			properties.add(player.getGameAddress().getAddress().getHostAddress());

			// Player's game port
			properties.add(player.getGameAddress().getPort());

			// Player's administrator status
			properties.add(player.isAdmin());

			// Player's mute status
			properties.add(player.isMute());

			// Player's deafen status
			properties.add(player.isDeafen());

			// Player's X coordinate
			properties.add(player.getPosition().getX());

			// Player's Y coordinate
			properties.add(player.getPosition().getY());

			// Player's Z coordinate
			properties.add(player.getPosition().getZ());

			// Player's yaw angle
			properties.add(player.getPosition().getYaw());

			// Player's pitch angle
			properties.add(player.getPosition().getPitch());
		}

		return create(getVersion(), Identifier.GET_PLAYER_INFO, properties.toArray());
	}

	@Override
	public IMumbleMessage onPlayerNameChange(String oldName, String newName) {
		return create(getVersion(), Identifier.SET_PLAYER_NAME, oldName, newName);
	}

	@Override
	public IMumbleMessage onPlayerOnlineChange(IPlayer player) {
		return create(getVersion(), Identifier.SET_PLAYER_ONLINE_STATUS, player.getName(), player.isOnline());
	}

	@Override
	public IMumbleMessage onPlayerGameAddressChange(IPlayer player) {
		return create(getVersion(), Identifier.SET_PLAYER_GAME_ADDRESS, player.getName(), player.getGameAddress().getAddress().getHostAddress(),
				player.getGameAddress().getPort());
	}

	@Override
	public IMumbleMessage onPlayerAdminChange(IPlayer player) {
		return create(getVersion(), Identifier.SET_PLAYER_ADMINISTRATOR, player.getName(), player.isAdmin());
	}

	@Override
	public IMumbleMessage onPlayerMuteChange(IPlayer player) {
		return create(getVersion(), Identifier.SET_PLAYER_MUTE, player.getName(), player.isMute());
	}

	@Override
	public IMumbleMessage onPlayerMuteByChange(IPlayer target, IPlayer source) {
		return create(getVersion(), Identifier.SET_PLAYER_MUTE_BY, target.getName(), source.getName(), target.isMuteBy(source));
	}

	@Override
	public IMumbleMessage onPlayerDeafenChange(IPlayer player) {
		return create(getVersion(), Identifier.SET_PLAYER_DEAFEN, player.getName(), player.isDeafen());
	}

	@Override
	public IMumbleMessage onPlayerKick(IPlayer kicked, IPlayer kicking) {
		return create(getVersion(), Identifier.KICK_PLAYER_FROM_CHANNEL, kicked.getName(), kicking.getName());
	}

	@Override
	public IMumbleMessage onPlayerPositionChange(IPlayer player) {
		return create(getVersion(), Identifier.SET_PLAYER_POSITION, player.getName(), player.getPosition().getX(), player.getPosition().getY(),
				player.getPosition().getZ(), player.getPosition().getYaw(), player.getPosition().getPitch());
	}

	@Override
	public IMumbleMessage onChannelPlayerAdd(IChannel channel, IPlayer player) {
		return create(getVersion(), Identifier.ADD_PLAYER_TO_CHANNEL, channel.getName(), player.getName());
	}

	@Override
	public IMumbleMessage onChannelPlayerRemove(IChannel channel, IPlayer player) {
		return create(getVersion(), Identifier.REMOVE_PLAYER_FROM_CHANNEL, channel.getName(), player.getName());
	}

	@Override
	public IMumbleMessage onParameterValueChange(IParameter<?> parameter) {
		List<Object> informations = new ArrayList<Object>();

		// Channel's name
		informations.add(parameter.getSoundModifier().getChannel().getName());

		// Parameter's name
		informations.add(parameter.getName());

		// Parameter's type
		informations.add(parameter.getType());

		// Parameter's value
		informations.add(parameter.getValue());

		return create(getVersion(), Identifier.SET_PARAMETER_VALUE, informations.toArray());
	}

	@Override
	public IMumbleMessage onParameterMinValueChange(IRangeParameter<?> parameter) {
		List<Object> informations = new ArrayList<Object>();

		// Channel's name
		informations.add(parameter.getSoundModifier().getChannel().getName());

		// Parameter's name
		informations.add(parameter.getName());

		// Parameter's type
		informations.add(parameter.getType());

		// Parameter's minimum value
		informations.add(parameter.getMin());

		return create(getVersion(), Identifier.SET_PARAMETER_MIN_VALUE, informations.toArray());
	}

	@Override
	public IMumbleMessage onParameterMaxValueChange(IRangeParameter<?> parameter) {
		List<Object> informations = new ArrayList<Object>();

		// Channel's name
		informations.add(parameter.getSoundModifier().getChannel().getName());

		// Parameter's name
		informations.add(parameter.getName());

		// Parameter's type
		informations.add(parameter.getType());

		// Parameter's maximum value
		informations.add(parameter.getMax());

		return create(getVersion(), Identifier.SET_PARAMETER_MAX_VALUE, informations.toArray());
	}

	@Override
	public IMumbleMessage onChannelSoundModifierChange(IChannel channel) {
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

			// Parameter's default value
			informations.add(parameter.getDefaultValue());

			// Parameter's range
			boolean isRange = parameter instanceof IRangeParameter<?>;
			informations.add(isRange);

			if (isRange) {
				IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;

				// Parameter's minimum value
				informations.add(rangeParameter.getMin());

				// Parameter's maximum value
				informations.add(rangeParameter.getMax());
			}
		}

		return create(getVersion(), Identifier.SET_CHANNEL_SOUND_MODIFIER, informations.toArray());
	}

	@Override
	public IMumbleMessage onGamePortCheck(int port) {
		return create(getVersion(), Identifier.IS_GAME_PORT_USED, port);
	}

	/**
	 * Creates a message that contains the current server configuration.
	 * 
	 * @param request The request sent by the remote in order to get the server configuration.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getFullServerConfiguration(GetFullServerConfigurationV10 request) {
		List<Object> informations = new ArrayList<Object>();

		List<IPlayer> players = getServer().getPlayers().toList();

		// Number of players
		informations.add(players.size());

		for (IPlayer player : players) {
			// Player's name
			informations.add(player.getName());

			// Player's identifier
			informations.add(player.getIdentifier());

			// Player's online status
			informations.add(player.isOnline());

			// Player's game address
			informations.add(player.getGameAddress().getAddress().getHostAddress());

			// Player's game port
			informations.add(player.getGameAddress().getPort());

			// Player's administrator status
			informations.add(player.isAdmin());

			// Player's mute status
			informations.add(player.isMute());

			// Player's deafen status
			informations.add(player.isDeafen());

			// Player's X coordinate
			informations.add(player.getPosition().getX());

			// Player's Y coordinate
			informations.add(player.getPosition().getY());

			// Player's Z coordinate
			informations.add(player.getPosition().getZ());

			// Player's yaw angle
			informations.add(player.getPosition().getYaw());

			// Player's pitch angle
			informations.add(player.getPosition().getPitch());

		}

		// Number of sound modifier
		Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
		informations.add(modifiers.size());

		// Modifier informations
		for (Map.Entry<String, ISoundModifier> modifierEntry : modifiers.entrySet()) {
			// Modifier's name
			informations.add(modifierEntry.getValue().getName());

			// Number of parameter
			informations.add(modifierEntry.getValue().getParameters().size());

			// Modifier's parameter
			for (IParameter<?> parameter : modifierEntry.getValue().getParameters()) {
				// Parameter's name
				informations.add(parameter.getName());

				// Parameter's type
				informations.add(parameter.getType());

				// Parameter's default value
				informations.add(parameter.getDefaultValue());

				// Parameter's value
				informations.add(parameter.getValue());

				// Parameter's range
				boolean isRange = parameter instanceof RangeParameter;
				informations.add(isRange);

				if (isRange) {
					IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
					informations.add(rangeParameter.getMin());
					informations.add(rangeParameter.getMax());
				}
			}
		}

		// Number of channels
		informations.add(getServer().getChannels().toList().size());
		for (IChannel channel : getServer().getChannels()) {
			// Channel name
			informations.add(channel.getName());

			// Channel's sound modifier name
			informations.add(channel.getSoundModifier().getName());

			// Number of parameters
			informations.add(channel.getSoundModifier().getParameters().size());

			for (IParameter<?> parameter : channel.getSoundModifier().getParameters()) {
				// Parameter's name
				informations.add(parameter.getName());

				// Parameter's type
				informations.add(parameter.getType());

				// Parameter's default value
				informations.add(parameter.getDefaultValue());

				// Parameter's value
				informations.add(parameter.getValue());

				// Parameter's range
				boolean isRange = parameter instanceof RangeParameter;
				informations.add(isRange);

				if (isRange) {
					IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
					informations.add(rangeParameter.getMin());
					informations.add(rangeParameter.getMax());
				}
			}

			// Number of players
			informations.add(channel.getPlayers().toList().size());

			for (IPlayer player : channel.getPlayers())
				// Player name
				informations.add(player.getName());
		}
		return answer(getVersion(), request, informations.toArray());
	}

	/**
	 * Creates a message that contains the current server configuration.
	 * 
	 * @param holder The holder that contains the connection that received the request and the request itself.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getServerConfiguration(RequestReceivedHolder holder) {
		List<Object> informations = new ArrayList<Object>();

		RunResult result = runIfInstanceof(holder, PlayerMumbleClient.class, client -> client.getPlayer() != null && client.getPlayer().isOnline());

		// Player's online status
		informations.add(result.getResult());

		if (result.getResult()) {
			IPlayer mainPlayer = ((PlayerMumbleClient) holder.getConnection()).getPlayer();

			// Player's name
			informations.add(mainPlayer.getName());

			// Player's identifier
			informations.add(mainPlayer.getIdentifier());

			// Player's game address
			informations.add(mainPlayer.getGameAddress().getAddress().getHostAddress());

			// Player's game port
			informations.add(mainPlayer.getGameAddress().getPort());

			// Player's administrator status
			informations.add(mainPlayer.isAdmin());

			// Player's mute status
			informations.add(mainPlayer.isMute());

			// Player's deafen status
			informations.add(mainPlayer.isDeafen());

			// Player's X coordinate
			informations.add(mainPlayer.getPosition().getX());

			// Player's Y coordinate
			informations.add(mainPlayer.getPosition().getY());

			// Player's Z coordinate
			informations.add(mainPlayer.getPosition().getZ());

			// Player's yaw angle
			informations.add(mainPlayer.getPosition().getYaw());

			// Player's pitch angle
			informations.add(mainPlayer.getPosition().getPitch());
		}

		// Number of sound modifier
		Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
		informations.add(modifiers.size());

		// Modifier informations
		for (Map.Entry<String, ISoundModifier> modifierEntry : modifiers.entrySet()) {
			// Modifier's name
			informations.add(modifierEntry.getValue().getName());

			// Number of parameter
			informations.add(modifierEntry.getValue().getParameters().size());

			// Modifier's parameter
			for (IParameter<?> parameter : modifierEntry.getValue().getParameters()) {
				// Parameter's name
				informations.add(parameter.getName());

				// Parameter's type
				informations.add(parameter.getType());

				// Parameter's default value
				informations.add(parameter.getDefaultValue());

				// Parameter's value
				informations.add(parameter.getValue());

				// Parameter's range
				boolean isRange = parameter instanceof RangeParameter;
				informations.add(isRange);

				if (isRange) {
					IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
					informations.add(rangeParameter.getMin());
					informations.add(rangeParameter.getMax());
				}
			}
		}

		// Number of channels
		informations.add(getServer().getChannels().toList().size());
		for (IChannel channel : getServer().getChannels()) {
			// Channel name
			informations.add(channel.getName());

			// Channel's sound modifier name
			informations.add(channel.getSoundModifier().getName());

			// Number of parameters
			informations.add(channel.getSoundModifier().getParameters().size());

			for (IParameter<?> parameter : channel.getSoundModifier().getParameters()) {
				// Parameter's name
				informations.add(parameter.getName());

				// Parameter's type
				informations.add(parameter.getType());

				// Parameter's default value
				informations.add(parameter.getDefaultValue());

				// Parameter's value
				informations.add(parameter.getValue());

				// Parameter's range
				boolean isRange = parameter instanceof RangeParameter;
				informations.add(isRange);

				if (isRange) {
					IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
					informations.add(rangeParameter.getMin());
					informations.add(rangeParameter.getMax());
				}
			}

			// Number of players
			informations.add(channel.getPlayers().toList().size());

			for (IPlayer player : channel.getPlayers())
				// Player name
				informations.add(player.getName());
		}
		return answer(getVersion(), holder.getRequest(), informations.toArray());
	}

	/**
	 * Adds a player on the server.
	 * 
	 * @param request The request sent by the server in order to add a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage registerPlayerOnServer(RegisterPlayerOnServerV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerInfo().getName());
		if (optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_ALREADY_REGISTERED);

		FullPlayerInfo info = request.getPlayerInfo();
		IPlayer player = getServer().getPlayers().add(info.getName(), info.getGameAddress(), info.isAdmin(), info.getX(), info.getY(), info.getZ(), info.getYaw(),
				info.getPitch());
		if (player == null)
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Removes a player on the server.
	 * 
	 * @param request The request sent by the server in order to add a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage unregisterPlayerOnServer(UnregisterPlayerFromServerV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		IPlayer player = getServer().getPlayers().remove(request.getPlayerName());
		if (player == null)
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Get all characteristics of the player associated to the connection that received the request.
	 * 
	 * @param holder The holder that contains the connection that received the request and the request itself.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerInfo(RequestReceivedHolder holder) {
		List<Object> informations = new ArrayList<Object>();

		RunResult result = runIfInstanceof(holder, PlayerMumbleClient.class, client -> client.getPlayer() != null && client.getPlayer().isOnline());

		// Player's online status
		informations.add(result.getResult());

		if (result.getResult()) {
			IPlayer mainPlayer = ((PlayerMumbleClient) holder.getConnection()).getPlayer();

			// Player's name
			informations.add(mainPlayer.getName());

			// Player's identifier
			informations.add(mainPlayer.getIdentifier());

			// Player's game address
			informations.add(mainPlayer.getGameAddress().getAddress().getHostAddress());

			// Player's game port
			informations.add(mainPlayer.getGameAddress().getPort());

			// Player's administrator status
			informations.add(mainPlayer.isAdmin());

			// Player's mute status
			informations.add(mainPlayer.isMute());

			// Player's deafen status
			informations.add(mainPlayer.isDeafen());

			// Player's X coordinate
			informations.add(mainPlayer.getPosition().getX());

			// Player's Y coordinate
			informations.add(mainPlayer.getPosition().getY());

			// Player's Z coordinate
			informations.add(mainPlayer.getPosition().getZ());

			// Player's yaw angle
			informations.add(mainPlayer.getPosition().getYaw());

			// Player's pitch angle
			informations.add(mainPlayer.getPosition().getPitch());
		}

		return answer(getVersion(), holder.getRequest(), informations.toArray());
	}

	/**
	 * Get the online status of a player.
	 * 
	 * @param request The request received from the remote in order to get the online status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerOnlineStatus(GetPlayerOnlineStatusV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		return answer(getVersion(), request, request.getPlayerName(), optPlayer.get().isOnline());
	}

	/**
	 * Set the online status of a player.
	 * 
	 * @param request The request received from the remote in order to update the online status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerOnlineStatus(SetPlayerOnlineStatusV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		optPlayer.get().setOnline(request.isOnline());
		if (optPlayer.get().isOnline() != request.isOnline())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Rename a player on the server.
	 * 
	 * @param request The request received from the remote in order to rename a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage renamePlayer(SetPlayerNameV10 request) {
		Optional<IPlayer> optOldPlayer = getServer().getPlayers().get(request.getOldName());
		if (!optOldPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		Optional<IPlayer> optNewPlayer = getServer().getPlayers().get(request.getNewName());
		if (optNewPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_ALREADY_EXISTS);

		optOldPlayer.get().setName(request.getNewName());
		if (!optOldPlayer.get().getName().equals(request.getNewName()))
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Get the game address of a player.
	 * 
	 * @param request The request received from the remote in order to get game address of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerGameAddress(GetPlayerGameAddressV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		String gameAddress = optPlayer.get().getGameAddress().getAddress().getHostAddress();
		int gamePort = optPlayer.get().getGameAddress().getPort();
		return answer(getVersion(), request, request.getPlayerName(), gameAddress, gamePort);
	}

	/**
	 * Update the game address of a player.
	 * 
	 * @param request The request received from the remote in order to update the game address of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerGameAddress(SetPlayerGameAddressV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		optPlayer.get().setGameAddress(request.getGameAddress());
		if (!optPlayer.get().getGameAddress().equals(request.getGameAddress()))
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Get the administrator status of a player.
	 * 
	 * @param request The request received from the remote in order to get the administrator status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerAdmin(GetPlayerAdministratorStatusV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		return answer(getVersion(), request, request.getPlayerName(), optPlayer.get().isAdmin());
	}

	/**
	 * Update the administrator status of a player.
	 * 
	 * @param request The request received from the remote in order to update the administrator status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerAdmin(SetPlayerAdministratorStatusV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		try {
			optPlayer.get().setAdmin(request.isAdmin());
		} catch (PlayerNotRegisteredInChannelException e) {
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optPlayer.get().isAdmin() != request.isAdmin())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Get the mute status of a player.
	 * 
	 * @param request The request received from the remote in order to get the mute status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerMute(GetPlayerMuteStatusV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		return answer(getVersion(), request, request.getPlayerName(), optPlayer.get().isMute());
	}

	/**
	 * Update the mute status of a player.
	 * 
	 * @param holder The holder that contains the connection that received the request and the request itself.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerMute(RequestReceivedHolder holder) {
		SetPlayerMuteStatusV10 request = (SetPlayerMuteStatusV10) holder.getRequest();
		RunResult result = runIfInstanceof(holder, PlayerMumbleClient.class, client -> client.getPlayer().getName().equals(request.getPlayerName()));
		Optional<IPlayer> optPlayer;

		// Case when the connection corresponds to a player connection -> Needs to check player's name match.
		if (result.getHasRun()) {
			if (!result.getResult())
				return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_DOES_NOT_MATCH);
			else
				optPlayer = Optional.of(((PlayerMumbleClient) holder.getConnection()).getPlayer());
		}
		// Case when the connection corresponds to a stand-alone connection -> Needs to check if the player exist.
		else {
			optPlayer = getServer().getPlayers().get(request.getPlayerName());
			if (!optPlayer.isPresent())
				return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_NOT_FOUND);
		}

		try {
			optPlayer.get().setMute(request.isMute());
		} catch (PlayerNotRegisteredInChannelException e) {
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optPlayer.get().isMute() != request.isMute())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Update the mute status of a player for another player.
	 * 
	 * @param request The request received from the remote in order to update the mute status of a player for another player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerMuteBy(SetPlayerMuteByStatusV10 request) {
		Optional<IPlayer> optTarget = getServer().getPlayers().get(request.getTarget());
		if (!optTarget.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		Optional<IPlayer> optSource = getServer().getPlayers().get(request.getSource());
		if (!optSource.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		optTarget.get().setMuteBy(optSource.get(), request.isMute());
		if (optTarget.get().isMuteBy(optSource.get()) != request.isMute())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Get the deafen status of a player.
	 * 
	 * @param request The request received from the remote in order to get the deafen status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerDeafen(GetPlayerDeafenStatusV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		return answer(getVersion(), request, request.getPlayerName(), optPlayer.get().isDeafen());
	}

	/**
	 * Update the deafen status of a player.
	 * 
	 * @param request The request received from the remote in order to update the deafen status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerDeafen(SetPlayerDeafenStatusV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		try {
			optPlayer.get().setDeafen(request.isDeafen());
		} catch (PlayerNotRegisteredInChannelException e) {
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optPlayer.get().isDeafen() != request.isDeafen())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Kicks a player from a channel.
	 * 
	 * @param request The request received from the remote in order to kick a player from a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage kickPlayerFromChannel(KickPlayerFromChannelV10 request) {
		Optional<IPlayer> optKickedPlayer = getServer().getPlayers().get(request.getKicked());
		if (!optKickedPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		Optional<IPlayer> optKickingPlayer = getServer().getPlayers().get(request.getKicking());
		if (!optKickingPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		try {
			optKickedPlayer.get().kick(optKickingPlayer.get());
		} catch (PlayerNotAdministratorException e) {
			return answer(getVersion(), request, ErrorCode.PERMISSION_REFUSED);
		} catch (PlayerNotRegisteredInChannelException e) {
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optKickedPlayer.get().getChannel() != null)
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Get the position of a player.
	 * 
	 * @param request The request sent by the remote in order to update the position of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerPosition(GetPlayerPositionV10 request) {
		String playerName = request.getPlayerInfo().getName();

		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerInfo().getName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		IPosition position = optPlayer.get().getPosition();
		return answer(getVersion(), request, playerName, position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch());
	}

	/**
	 * Update the position of a player.
	 * 
	 * @param request The request sent by the remote in order to update the position of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerPosition(SetPlayerPositionV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return answer(getVersion(), request, ErrorCode.PLAYER_NOT_FOUND);

		optPlayer.get().getPosition().update(request.getX(), request.getY(), request.getZ(), request.getYaw(), request.getPitch());
		if (optPlayer.get().getPosition().getX() != request.getX() || optPlayer.get().getPosition().getY() != request.getY()
				|| optPlayer.get().getPosition().getZ() != request.getZ() || optPlayer.get().getPosition().getYaw() != request.getYaw()
				|| optPlayer.get().getPosition().getPitch() != request.getPitch())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Gets the list of channels registered on the server.
	 * 
	 * @param request The request sent by the remote in order to get the channels list.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getChannelsInfo(GetChannelsInfoV10 request) {
		List<Object> informations = new ArrayList<Object>();

		// Number of channels
		informations.add(getServer().getChannels().toList().size());

		for (IChannel channel : getServer().getChannels()) {
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

				// Parameter's default value
				informations.add(parameter.getDefaultValue());

				// Parameter's value
				informations.add(parameter.getValue());

				// Parameter's range
				boolean isRange = parameter instanceof RangeParameter;
				informations.add(isRange);

				if (isRange) {
					IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
					informations.add(rangeParameter.getMin());
					informations.add(rangeParameter.getMax());
				}
			}

			// Number of players
			informations.add(channel.getPlayers().toList().size());

			for (IPlayer player : channel.getPlayers()) {
				// Player's name
				informations.add(player.getName());

				// Player's mute
				informations.add(player.isMute());

				// Player's deafen
				informations.add(player.isDeafen());
			}
		}
		return answer(getVersion(), request, informations.toArray());
	}

	/**
	 * Gets information about a channel
	 * 
	 * @param request The request sent by the remote in order to get information about a channel
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getChannelInfo(GetChannelInfoV10 request) {
		List<Object> informations = new ArrayList<Object>();

		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		// Channel's name
		informations.add(optChannel.get().getName());

		// Modifier's name
		informations.add(optChannel.get().getSoundModifier().getName());

		// Number of parameters
		informations.add(optChannel.get().getSoundModifier().getParameters().size());

		for (IParameter<?> parameter : optChannel.get().getSoundModifier().getParameters()) {
			// Parameter's name
			informations.add(parameter.getName());

			// Parameter's type
			informations.add(parameter.getType());

			// Parameter's default value
			informations.add(parameter.getDefaultValue());

			// Parameter's value
			informations.add(parameter.getValue());

			// Parameter's range
			boolean isRange = parameter instanceof RangeParameter;
			informations.add(isRange);

			if (isRange) {
				IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
				informations.add(rangeParameter.getMin());
				informations.add(rangeParameter.getMax());
			}
		}

		// Number of players
		informations.add(optChannel.get().getPlayers().toList().size());

		for (IPlayer player : optChannel.get().getPlayers()) {
			// Player's name
			informations.add(player.getName());

			// Player's mute
			informations.add(player.isMute());

			// Player's deafen
			informations.add(player.isDeafen());
		}
		return answer(getVersion(), request, informations.toArray());
	}

	/**
	 * Adds a channel to this server based on the properties of the given request.
	 * 
	 * @param request The request sent by the remote in order to add a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage registerChannelOnServer(RegisterChannelOnServerV10 request) {
		if (getServer().getChannels().get(request.getChannelInfo().getName()).isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_ALREADY_EXISTS);

		Optional<ISoundModifier> optModifier = SoundManager.getByName(request.getChannelInfo().getSoundModifierInfo().getName());
		if (!optModifier.isPresent())
			return answer(getVersion(), request, ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

		ParameterList parameterList = new ParameterList();
		for (FullParameterInfo parameterInfo : request.getChannelInfo().getSoundModifierInfo().getParameterInfo().values())
			parameterList.add(parameterInfo);

		IChannel channel = getServer().getChannels().add(request.getChannelInfo().getName(), request.getChannelInfo().getSoundModifierInfo().getName());
		if (channel == null)
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		channel.getSoundModifier().getParameters().update(parameterList);
		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Removes a channel from this server based on the properties of the given request.
	 * 
	 * @param request The request sent by the remote in order to remove a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage unregisterChannelFromServer(UnregisterChannelFromServerV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		if (getServer().getChannels().remove(request.getChannelName()) == null)
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Renames a channel based on the properties of the given request.
	 * 
	 * @param request The request sent by the remote in order to rename a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage renameChannel(SetChannelNameV10 request) {
		Optional<IChannel> optOldChannel = getServer().getChannels().get(request.getOldName());
		if (!optOldChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IChannel> optNewChannel = getServer().getChannels().get(request.getNewName());
		if (optNewChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_ALREADY_EXISTS);

		optOldChannel.get().setName(request.getNewName());
		if (!optOldChannel.get().getName().equals(request.getNewName()))
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Adds a player to a channel.
	 * 
	 * @param holder The holder that contains the connection that received the request and the request itself.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage addPlayerToChannel(RequestReceivedHolder holder) {
		AddPlayerToChannelV10 request = (AddPlayerToChannelV10) holder.getRequest();
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), holder.getRequest(), ErrorCode.CHANNEL_NOT_FOUND);

		RunResult result = runIfInstanceof(holder, PlayerMumbleClient.class, client -> client.getPlayer().getName().equals(request.getPlayerName()));
		Optional<IPlayer> optPlayer;

		// Case when the connection corresponds to a player connection -> Needs to check player's name match.
		if (result.getHasRun()) {
			if (!result.getResult())
				return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_DOES_NOT_MATCH);
			else
				optPlayer = Optional.of(((PlayerMumbleClient) holder.getConnection()).getPlayer());
		}
		// Case when the connection corresponds to a stand-alone connection -> Needs to check if the player exist.
		else {
			optPlayer = getServer().getPlayers().get(request.getPlayerName());
			if (!optPlayer.isPresent())
				return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_NOT_FOUND);
		}

		if (getServer().getPlayers().getPlayersInChannel().contains(optPlayer.get()))
			return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_ALREADY_REGISTERED);

		try {
			optChannel.get().getPlayers().add(optPlayer.get());
		} catch (PlayerMumbleClientNotJoinedException e) {
			return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_CLIENT_NOT_JOINED);
		}

		if (!optChannel.get().getPlayers().toList().contains(optPlayer.get()))
			return answer(getVersion(), holder.getRequest(), ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), holder.getRequest(), holder.getRequest().getProperties());
	}

	/**
	 * Removes a player from a channel.
	 * 
	 * @param holder The holder that contains the connection that received the request and the request itself.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage removePlayerFromChannel(RequestReceivedHolder holder) {
		RemovePlayerFromChannelV10 request = (RemovePlayerFromChannelV10) holder.getRequest();
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		RunResult result = runIfInstanceof(holder, PlayerMumbleClient.class, client -> client.getPlayer().getName().equals(request.getPlayerName()));
		Optional<IPlayer> optPlayer;

		// Case when the connection corresponds to a player connection -> Needs to check player's name match.
		if (result.getHasRun()) {
			if (!result.getResult())
				return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_DOES_NOT_MATCH);
			else
				optPlayer = Optional.of(((PlayerMumbleClient) holder.getConnection()).getPlayer());
		}
		// Case when the connection corresponds to a stand-alone connection -> Needs to check if the player exist.
		else {
			optPlayer = getServer().getPlayers().get(request.getPlayerName());
			if (!optPlayer.isPresent())
				return answer(getVersion(), holder.getRequest(), ErrorCode.PLAYER_NOT_FOUND);
		}

		optChannel.get().getPlayers().remove(optPlayer.get());
		if (optChannel.get().getPlayers().toList().contains(optPlayer.get()))
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}

	/**
	 * Get the value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to update the value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getParameterValue(GetParameterValueV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return answer(getVersion(), request, ErrorCode.PARAMETER_NOT_FOUND);

		return answer(getVersion(), request, optChannel.get().getName(), optParameter.get().getName(), optParameter.get().getType(), optParameter.get().getValue());
	}

	/**
	 * Update the value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to update the value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setParameterValue(SetParameterValueV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return answer(getVersion(), request, ErrorCode.PARAMETER_NOT_FOUND);

		optParameter.get().setValue(request.getNewValue());
		if (optParameter.get().getValue() != request.getNewValue())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, optChannel.get().getName(), optParameter.get().getName(), optParameter.get().getType(), request.getNewValue());
	}

	/**
	 * Get the minimum value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to get the minimum value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getParameterMinValue(GetParameterMinValueV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return answer(getVersion(), request, ErrorCode.PARAMETER_NOT_FOUND);

		if (!(optParameter.get() instanceof IRangeParameter<?>))
			return answer(getVersion(), request, ErrorCode.PARAMETER_WITHOUT_MIN);

		IRangeParameter<?> range = (IRangeParameter<?>) optParameter.get();
		return answer(getVersion(), request, optChannel.get().getName(), range.getName(), range.getType(), range.getMin());
	}

	/**
	 * Update the minimum value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to update the minimum value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setParameterMinValue(SetParameterMinValueV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return answer(getVersion(), request, ErrorCode.PARAMETER_NOT_FOUND);

		if (!(optParameter.get() instanceof IRangeParameter<?>))
			return answer(getVersion(), request, ErrorCode.PARAMETER_WITHOUT_MIN);

		IRangeParameter<?> range = (IRangeParameter<?>) optParameter.get();
		range.setMin(request.getNewMinValue());
		if (range.getMin() != request.getNewMinValue())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, optChannel.get().getName(), optParameter.get().getName(), optParameter.get().getType(), request.getNewMinValue());
	}

	/**
	 * Get the maximum value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to get the maximum value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getParameterMaxValue(GetParameterMaxValueV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return answer(getVersion(), request, ErrorCode.PARAMETER_NOT_FOUND);

		if (!(optParameter.get() instanceof IRangeParameter<?>))
			return answer(getVersion(), request, ErrorCode.PARAMETER_WITHOUT_MAX);

		IRangeParameter<?> range = (IRangeParameter<?>) optParameter.get();

		return answer(getVersion(), request, optChannel.get().getName(), range.getName(), range.getType(), range.getMax());
	}

	/**
	 * Update the maximum value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to update the maximum value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setParameterMaxValue(SetParameterMaxValueV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return answer(getVersion(), request, ErrorCode.PARAMETER_NOT_FOUND);

		if (!(optParameter.get() instanceof IRangeParameter<?>))
			return answer(getVersion(), request, ErrorCode.PARAMETER_WITHOUT_MAX);

		IRangeParameter<?> range = (IRangeParameter<?>) optParameter.get();
		range.setMax(request.getNewMaxValue());
		if (range.getMax() != request.getNewMaxValue())
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, optChannel.get().getName(), optParameter.get().getName(), optParameter.get().getType(), request.getNewMaxValue());
	}

	/**
	 * Get information about all sound modifiers registered on the server
	 * 
	 * @param request The request sent by the remote in order to get information about all sound modifiers.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getSoundModifiersInfo(GetSoundModifiersInfoV10 request) {
		List<Object> informations = new ArrayList<Object>();

		// Number of sound modifier
		Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
		informations.add(modifiers.size());

		// Modifier informations
		for (Map.Entry<String, ISoundModifier> modifierEntry : modifiers.entrySet()) {
			// Modifier's name
			informations.add(modifierEntry.getValue().getName());

			// Number of parameter
			informations.add(modifierEntry.getValue().getParameters().size());

			// Modifier's parameter
			for (IParameter<?> parameter : modifierEntry.getValue().getParameters()) {
				// Parameter's name
				informations.add(parameter.getName());

				// Parameter's type
				informations.add(parameter.getType());

				// Parameter's default value
				informations.add(parameter.getDefaultValue());

				// Parameter's value
				informations.add(parameter.getValue());

				// Parameter's range
				boolean isRange = parameter instanceof RangeParameter;
				informations.add(isRange);

				if (isRange) {
					IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
					informations.add(rangeParameter.getMin());
					informations.add(rangeParameter.getMax());
				}
			}
		}

		return answer(getVersion(), request, informations);
	}

	/**
	 * Get the sound modifier of a channel.
	 * 
	 * @param request The request sent by the remote in order to get the sound modifier of a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getChannelSoundModifier(GetChannelSoundModifierV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		List<Object> informations = new ArrayList<Object>();

		// Channel name
		informations.add(optChannel.get().getName());

		// Channel's sound modifier name
		informations.add(optChannel.get().getSoundModifier().getName());

		// Number of parameters
		informations.add(optChannel.get().getSoundModifier().getParameters().size());

		for (IParameter<?> parameter : optChannel.get().getSoundModifier().getParameters()) {
			// Parameter's name
			informations.add(parameter.getName());

			// Parameter's type
			informations.add(parameter.getType());

			// Parameter's default value
			informations.add(parameter.getDefaultValue());

			// Parameter's value
			informations.add(parameter.getValue());

			// Parameter's range
			boolean isRange = parameter instanceof RangeParameter;
			informations.add(isRange);

			if (isRange) {
				IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
				informations.add(rangeParameter.getMin());
				informations.add(rangeParameter.getMax());
			}
		}

		// Number of players
		informations.add(optChannel.get().getPlayers().toList().size());

		for (IPlayer player : optChannel.get().getPlayers())
			// Player name
			informations.add(player.getName());

		return answer(getVersion(), request, informations);
	}

	/**
	 * Set the sound modifier of a channel.
	 * 
	 * @param request The request sent by the remote in order to set the sound modifier of a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setChannelSoundModifier(SetChannelSoundModifierV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelInfo().getName());
		if (!optChannel.isPresent())
			return answer(getVersion(), request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<ISoundModifier> optModifier = SoundManager.getByName(request.getChannelInfo().getSoundModifierInfo().getName());
		if (!optModifier.isPresent())
			return answer(getVersion(), request, ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

		ParameterList parameterList = new ParameterList();
		for (FullParameterInfo parameterInfo : request.getChannelInfo().getSoundModifierInfo().getParameterInfo().values())
			parameterList.add(parameterInfo);

		optModifier.get().getParameters().update(parameterList);
		optChannel.get().setSoundModifier(optModifier.get());

		if (!optChannel.get().getSoundModifier().equals(optModifier.get()))
			return answer(getVersion(), request, ErrorCode.REQUEST_CANCELLED);

		return answer(getVersion(), request, request.getProperties());
	}
}
