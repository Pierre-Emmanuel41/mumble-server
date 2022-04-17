package fr.pederobien.mumble.server.impl.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.common.impl.messages.v10.ChannelsAddMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ChannelsGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ChannelsPlayerAddMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ChannelsPlayerRemoveMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ChannelsRemoveMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ChannelsSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ParameterMaxValueSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ParameterMinValueSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ParameterValueSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerAddMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerAdminSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerDeafenSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerGameAddressGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerGameAddressSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerKickSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerMuteBySetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerMuteSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerNameSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerOnlineGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerOnlineSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerPositionGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerPositionSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerRemoveMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.PlayerSetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.ServerInfoGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.SoundModifierGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.SoundModifierInfoMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.SoundModifierSetMessageV10;
import fr.pederobien.mumble.common.impl.model.ParameterInfo.FullParameterInfo;
import fr.pederobien.mumble.common.impl.model.PlayerInfo.FullPlayerInfo;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.exceptions.PlayerNotAdministratorException;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
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
		super(server);

		// Server info map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> serverInfoMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		serverInfoMap.put(Oid.GET, request -> getServerConfiguration((ServerInfoGetMessageV10) request));
		getRequests().put(Idc.SERVER_INFO, serverInfoMap);

		// Channels map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> channelsMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		channelsMap.put(Oid.GET, request -> getChannels((ChannelsGetMessageV10) request));
		channelsMap.put(Oid.ADD, request -> addChannel((ChannelsAddMessageV10) request));
		channelsMap.put(Oid.REMOVE, request -> removeChannel((ChannelsRemoveMessageV10) request));
		channelsMap.put(Oid.SET, request -> renameChannel((ChannelsSetMessageV10) request));
		getRequests().put(Idc.CHANNELS, channelsMap);

		// Player map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerMap.put(Oid.GET, request -> playerInfoGet((PlayerGetMessageV10) request));
		playerMap.put(Oid.SET, request -> playerInfoSet((PlayerSetMessageV10) request));
		playerMap.put(Oid.ADD, request -> addPlayer((PlayerAddMessageV10) request));
		playerMap.put(Oid.REMOVE, request -> removePlayer((PlayerRemoveMessageV10) request));
		getRequests().put(Idc.PLAYER, playerMap);

		// Player name map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerNameMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerNameMap.put(Oid.SET, request -> renamePlayer((PlayerNameSetMessageV10) request));
		getRequests().put(Idc.PLAYER_NAME, playerNameMap);

		// Player online map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerOnlineMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerOnlineMap.put(Oid.GET, request -> getPlayerOnlineStatus((PlayerOnlineGetMessageV10) request));
		playerOnlineMap.put(Oid.SET, request -> setPlayerOnlineStatus((PlayerOnlineSetMessageV10) request));
		getRequests().put(Idc.PLAYER_ONLINE, playerOnlineMap);

		// Player game address map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerGameAddressMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerGameAddressMap.put(Oid.GET, request -> getPlayerGameAddress((PlayerGameAddressGetMessageV10) request));
		playerGameAddressMap.put(Oid.SET, request -> setPlayerGameAddress((PlayerGameAddressSetMessageV10) request));
		getRequests().put(Idc.PLAYER_GAME_ADDRESS, playerGameAddressMap);

		// Player administrator map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerAdminMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerAdminMap.put(Oid.SET, request -> setPlayerAdmin((PlayerAdminSetMessageV10) request));
		getRequests().put(Idc.PLAYER_ADMIN, playerAdminMap);

		// Player mute map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerMuteMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerMuteMap.put(Oid.SET, request -> setPlayerMute((PlayerMuteSetMessageV10) request));
		getRequests().put(Idc.PLAYER_MUTE, playerMuteMap);

		// Player mute by map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerMuteByMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerMuteByMap.put(Oid.SET, request -> setPlayerMuteBy((PlayerMuteBySetMessageV10) request));
		getRequests().put(Idc.PLAYER_MUTE_BY, playerMuteByMap);

		// Player deafen map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerDeafenMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerDeafenMap.put(Oid.SET, request -> setPlayerDeafen((PlayerDeafenSetMessageV10) request));
		getRequests().put(Idc.PLAYER_DEAFEN, playerDeafenMap);

		// Player kick map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerKickMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerKickMap.put(Oid.SET, request -> kickPlayerFromChannel((PlayerKickSetMessageV10) request));
		getRequests().put(Idc.PLAYER_KICK, playerKickMap);

		// Player position map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerPositionMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerPositionMap.put(Oid.GET, request -> getPlayerPosition((PlayerPositionGetMessageV10) request));
		playerPositionMap.put(Oid.SET, request -> setPlayerPosition((PlayerPositionSetMessageV10) request));
		getRequests().put(Idc.PLAYER_POSITION, playerPositionMap);

		// Channels player map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> channelsPlayerMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		channelsPlayerMap.put(Oid.ADD, request -> addPlayerToChannel((ChannelsPlayerAddMessageV10) request));
		channelsPlayerMap.put(Oid.REMOVE, request -> removePlayerFromChannel((ChannelsPlayerRemoveMessageV10) request));
		getRequests().put(Idc.CHANNELS_PLAYER, channelsPlayerMap);

		// Parameter value map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> parameterValueMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		parameterValueMap.put(Oid.SET, request -> setParameterValue((ParameterValueSetMessageV10) request));
		getRequests().put(Idc.PARAMETER_VALUE, parameterValueMap);

		// Parameter minimum value map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> parameterMinValueMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		parameterMinValueMap.put(Oid.SET, request -> setParameterMinValue((ParameterMinValueSetMessageV10) request));
		getRequests().put(Idc.PARAMETER_MIN_VALUE, parameterMinValueMap);

		// Parameter maximum value map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> parameterMaxValueMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		parameterMaxValueMap.put(Oid.SET, request -> setParameterMaxValue((ParameterMaxValueSetMessageV10) request));
		getRequests().put(Idc.PARAMETER_MAX_VALUE, parameterMaxValueMap);

		// Sound modifier map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> soundModifierMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		soundModifierMap.put(Oid.GET, request -> soundModifierGet((SoundModifierGetMessageV10) request));
		soundModifierMap.put(Oid.SET, request -> setChannelSoundModifier((SoundModifierSetMessageV10) request));
		soundModifierMap.put(Oid.INFO, request -> soundModifierInfo((SoundModifierInfoMessageV10) request));
		getRequests().put(Idc.SOUND_MODIFIER, soundModifierMap);
	}

	@Override
	protected IMumbleMessage playerInfoGet(PlayerGetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerInfo().getName());
		if (optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, optPlayer.get().isOnline(), optPlayer.get().getName(), optPlayer.get().isAdmin());
		return MumbleServerMessageFactory.answer(request, false);
	}

	@Override
	protected IMumbleMessage playerInfoSet(PlayerSetMessageV10 request) {
		/*
		 * if (request.getPlayerInfo().isOnline()) { String address = request.getPlayerInfo().getGameAddress(); int port =
		 * request.getPlayerInfo().getGamePort(); boolean isAdmin = request.getPlayerInfo().isAdmin(); try {
		 * getServer().getClients().addPlayer(new InetSocketAddress(InetAddress.getByName(address), port),
		 * request.getPlayerInfo().getName(), isAdmin); } catch (UnknownHostException e) { return
		 * MumbleServerMessageFactory.answer(request, ErrorCode.UNEXPECTED_ERROR); } } else
		 * getServer().getClients().removePlayer(request.getPlayerInfo().getName()); return MumbleServerMessageFactory.answer(request,
		 * request.getProperties());
		 */
		return null;
	}

	@Override
	protected IMumbleMessage soundModifierGet(SoundModifierGetMessageV10 request) {
		List<Object> informations = new ArrayList<Object>();

		// channel's name
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		// channel's name
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

			// Parameter's value
			informations.add(parameter.getValue());
		}
		return MumbleServerMessageFactory.answer(request, informations.toArray());
	}

	@Override
	protected IMumbleMessage soundModifierInfo(SoundModifierInfoMessageV10 request) {
		List<Object> informations = new ArrayList<Object>();

		// Number of modifiers
		Map<String, ISoundModifier> modifiers = SoundManager.getSoundModifiers();
		informations.add(modifiers.size());

		// Modifier informations
		for (ISoundModifier modifier : modifiers.values()) {
			// Modifier's name
			informations.add(modifier.getName());

			// Number of parameter
			informations.add(modifier.getParameters().size());

			// Modifier's parameter
			for (IParameter<?> parameter : modifier.getParameters()) {
				// Parameter's name
				informations.add(parameter.getName());

				// Parameter's type
				informations.add(parameter.getType());

				// isRangeParameter
				boolean isRange = parameter instanceof RangeParameter;
				informations.add(isRange);

				// Parameter's default value
				informations.add(parameter.getDefaultValue());

				// Parameter's value
				informations.add(parameter.getValue());

				// Parameter's range value
				if (isRange) {
					RangeParameter<?> rangeParameter = (RangeParameter<?>) parameter;
					informations.add(rangeParameter.getMin());
					informations.add(rangeParameter.getMax());
				}
			}
		}

		return MumbleServerMessageFactory.answer(request, informations.toArray());
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

		return create(Idc.CHANNELS, Oid.ADD, informations.toArray());
	}

	@Override
	public IMumbleMessage onChannelRemove(IChannel channel) {
		return create(Idc.CHANNELS, Oid.REMOVE, channel.getName());
	}

	@Override
	public IMumbleMessage onChannelNameChange(IChannel channel, String oldName) {
		return create(Idc.CHANNELS, Oid.SET, oldName, channel.getName());
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

		return create(Idc.PLAYER, Oid.ADD, properties.toArray());
	}

	@Override
	public IMumbleMessage onServerPlayerRemove(String name) {
		return create(Idc.PLAYER, Oid.REMOVE, name);
	}

	@Override
	public IMumbleMessage onPlayerNameChange(String oldName, String newName) {
		return create(Idc.PLAYER_NAME, Oid.SET, oldName, newName);
	}

	@Override
	public IMumbleMessage onPlayerOnlineChange(IPlayer player) {
		return create(Idc.PLAYER_ONLINE, Oid.SET, player.getName(), player.isOnline());
	}

	@Override
	public IMumbleMessage onPlayerGameAddressChange(IPlayer player) {
		return create(Idc.PLAYER_GAME_ADDRESS, Oid.SET, player.getName(), player.getGameAddress().getAddress().getHostAddress(), player.getGameAddress().getPort());
	}

	@Override
	public IMumbleMessage onPlayerAdminChange(IPlayer player) {
		return create(Idc.PLAYER_ADMIN, Oid.SET, player.getName(), player.isAdmin());
	}

	@Override
	public IMumbleMessage onPlayerMuteChange(IPlayer player) {
		return create(Idc.PLAYER_MUTE, Oid.SET, player.getName(), player.isMute());
	}

	@Override
	public IMumbleMessage onPlayerMuteByChange(IPlayer target, IPlayer source) {
		return create(Idc.PLAYER_MUTE_BY, Oid.SET, target.getName(), source.getName(), target.isMuteBy(source));
	}

	@Override
	public IMumbleMessage onPlayerDeafenChange(IPlayer player) {
		return create(Idc.PLAYER_DEAFEN, Oid.SET, player.getName(), player.isDeafen());
	}

	@Override
	public IMumbleMessage onPlayerKick(IPlayer kicked, IPlayer kicking) {
		return create(Idc.PLAYER_KICK, Oid.SET, kicked.getName(), kicking.getName());
	}

	@Override
	public IMumbleMessage onPlayerPositionChange(IPlayer player) {
		return create(Idc.PLAYER_POSITION, Oid.SET, player.getName(), player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ(),
				player.getPosition().getYaw(), player.getPosition().getPitch());
	}

	@Override
	public IMumbleMessage onChannelPlayerAdd(IChannel channel, IPlayer player) {
		return create(Idc.CHANNELS_PLAYER, Oid.ADD, channel.getName(), player.getName());
	}

	@Override
	public IMumbleMessage onChannelPlayerRemove(IChannel channel, IPlayer player) {
		return create(Idc.CHANNELS_PLAYER, Oid.REMOVE, channel.getName(), player.getName());
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

		return create(Idc.PARAMETER_VALUE, Oid.SET, informations.toArray());
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

		return create(Idc.PARAMETER_MIN_VALUE, Oid.SET, informations.toArray());
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

		return create(Idc.PARAMETER_MAX_VALUE, Oid.SET, informations.toArray());
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

		return create(Idc.SOUND_MODIFIER, Oid.SET, informations.toArray());
	}

	/**
	 * Creates a message that contains the current server configuration.
	 * 
	 * @param request The request sent by the remote in order to get the server configuration.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getServerConfiguration(ServerInfoGetMessageV10 request) {
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
		return MumbleServerMessageFactory.answer(request, informations.toArray());
	}

	/**
	 * Gets the list of channels registered on the server.
	 * 
	 * @param request The request sent by the remote in order to get the channels list.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getChannels(ChannelsGetMessageV10 request) {
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
		return MumbleServerMessageFactory.answer(request, informations.toArray());
	}

	/**
	 * Adds a channel to this server based on the properties of the given request.
	 * 
	 * @param request The request sent by the remote in order to add a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage addChannel(ChannelsAddMessageV10 request) {
		if (getServer().getChannels().get(request.getChannelInfo().getName()).isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_ALREADY_EXISTS);

		Optional<ISoundModifier> optModifier = SoundManager.getByName(request.getChannelInfo().getSoundModifierInfo().getName());
		if (!optModifier.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

		ParameterList parameterList = new ParameterList();
		for (FullParameterInfo parameterInfo : request.getChannelInfo().getSoundModifierInfo().getParameterInfo().values())
			parameterList.add(parameterInfo);

		IChannel channel = getServer().getChannels().add(request.getChannelInfo().getName(), request.getChannelInfo().getSoundModifierInfo().getName());
		if (channel == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		channel.getSoundModifier().getParameters().update(parameterList);
		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Removes a channel from this server based on the properties of the given request.
	 * 
	 * @param request The request sent by the remote in order to remove a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage removeChannel(ChannelsRemoveMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		if (getServer().getChannels().remove(request.getChannelName()) == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Renames a channel based on the properties of the given request.
	 * 
	 * @param request The request sent by the remote in order to rename a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage renameChannel(ChannelsSetMessageV10 request) {
		Optional<IChannel> optOldChannel = getServer().getChannels().get(request.getOldName());
		if (!optOldChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IChannel> optNewChannel = getServer().getChannels().get(request.getNewName());
		if (optNewChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_ALREADY_EXISTS);

		optOldChannel.get().setName(request.getNewName());
		if (!optOldChannel.get().getName().equals(request.getNewName()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Adds a player on the server.
	 * 
	 * @param request The request sent by the server in order to add a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage addPlayer(PlayerAddMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerInfo().getName());
		if (optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_ALREADY_REGISTERED);

		FullPlayerInfo info = request.getPlayerInfo();
		IPlayer player = getServer().getPlayers().add(info.getName(), info.getGameAddress(), info.isAdmin(), info.getX(), info.getY(), info.getZ(), info.getYaw(),
				info.getPitch());
		if (player == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Removes a player on the server.
	 * 
	 * @param request The request sent by the server in order to add a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage removePlayer(PlayerRemoveMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		IPlayer player = getServer().getPlayers().remove(request.getPlayerName());
		if (player == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Rename a player on the server.
	 * 
	 * @param request The request received from the remote in order to rename a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage renamePlayer(PlayerNameSetMessageV10 request) {
		Optional<IPlayer> optOldPlayer = getServer().getPlayers().get(request.getOldName());
		if (!optOldPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		Optional<IPlayer> optNewPlayer = getServer().getPlayers().get(request.getNewName());
		if (optNewPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_ALREADY_EXISTS);

		optOldPlayer.get().setName(request.getNewName());
		if (!optOldPlayer.get().getName().equals(request.getNewName()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Get the online status of a player.
	 * 
	 * @param request The request received from the remote in order to get the online status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerOnlineStatus(PlayerOnlineGetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		return MumbleServerMessageFactory.answer(request, request.getPlayerName(), optPlayer.get().isOnline());
	}

	/**
	 * Set the online status of a player.
	 * 
	 * @param request The request received from the remote in order to update the online status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerOnlineStatus(PlayerOnlineSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		optPlayer.get().setOnline(request.isOnline());
		if (optPlayer.get().isOnline() != request.isOnline())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Get the game address of a player.
	 * 
	 * @param request The request received from the remote in order to get game address of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerGameAddress(PlayerGameAddressGetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		String gameAddress = optPlayer.get().getGameAddress().getAddress().getHostAddress();
		int gamePort = optPlayer.get().getGameAddress().getPort();
		return MumbleServerMessageFactory.answer(request, request.getPlayerName(), gameAddress, gamePort);
	}

	/**
	 * Update the game address of a player.
	 * 
	 * @param request The request received from the remote in order to update the game address of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerGameAddress(PlayerGameAddressSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		optPlayer.get().setGameAddress(request.getGameAddress());
		if (!optPlayer.get().getGameAddress().equals(request.getGameAddress()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Update the administrator status of a player.
	 * 
	 * @param request The request received from the remote in order to update the administrator status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerAdmin(PlayerAdminSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		try {
			optPlayer.get().setAdmin(request.isAdmin());
		} catch (PlayerNotRegisteredInChannelException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optPlayer.get().isAdmin() != request.isAdmin())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Update the mute status of a player.
	 * 
	 * @param request The request received from the remote in order to update the mute status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerMute(PlayerMuteSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		try {
			optPlayer.get().setMute(request.isMute());
		} catch (PlayerNotRegisteredInChannelException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optPlayer.get().isMute() != request.isMute())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Update the deafen status of a player.
	 * 
	 * @param request The request received from the remote in order to update the deafen status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerDeafen(PlayerDeafenSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		try {
			optPlayer.get().setDeafen(request.isDeafen());
		} catch (PlayerNotRegisteredInChannelException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optPlayer.get().isDeafen() != request.isDeafen())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Update the mute status of a player for another player.
	 * 
	 * @param request The request received from the remote in order to update the mute status of a player for another player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerMuteBy(PlayerMuteBySetMessageV10 request) {
		Optional<IPlayer> optTarget = getServer().getPlayers().get(request.getTarget());
		if (!optTarget.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		Optional<IPlayer> optSource = getServer().getPlayers().get(request.getSource());
		if (!optSource.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		optTarget.get().setMuteBy(optSource.get(), request.isMute());
		if (optTarget.get().isMuteBy(optSource.get()) != request.isMute())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Adds a player to a channel.
	 * 
	 * @param request The request received from the remote in order to add a player to a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage addPlayerToChannel(ChannelsPlayerAddMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		final Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		if (getServer().getPlayers().getPlayersInChannel().contains(optPlayer.get()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_ALREADY_REGISTERED);

		optChannel.get().getPlayers().add(optPlayer.get());
		if (!optChannel.get().getPlayers().toList().contains(optPlayer.get()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Removes a player from a channel.
	 * 
	 * @param request The request received from the remote in order to remove a player from a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage removePlayerFromChannel(ChannelsPlayerRemoveMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		final Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		optChannel.get().getPlayers().remove(optPlayer.get());
		if (optChannel.get().getPlayers().toList().contains(optPlayer.get()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Kicks a player from a channel.
	 * 
	 * @param request The request received from the remote in order to kick a player from a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage kickPlayerFromChannel(PlayerKickSetMessageV10 request) {
		Optional<IPlayer> optKickedPlayer = getServer().getPlayers().get(request.getKicked());
		if (!optKickedPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		Optional<IPlayer> optKickingPlayer = getServer().getPlayers().get(request.getKicking());
		if (!optKickingPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		try {
			optKickedPlayer.get().kick(optKickingPlayer.get());
		} catch (PlayerNotAdministratorException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED);
		} catch (PlayerNotRegisteredInChannelException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optKickedPlayer.get().getChannel() != null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Get the position of a player.
	 * 
	 * @param request The request sent by the remote in order to update the position of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage getPlayerPosition(PlayerPositionGetMessageV10 request) {
		String playerName = request.getPlayerInfo().getName();

		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerInfo().getName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		IPosition position = optPlayer.get().getPosition();
		return MumbleServerMessageFactory.answer(request, playerName, position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch());
	}

	/**
	 * Update the position of a player.
	 * 
	 * @param request The request sent by the remote in order to update the position of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerPosition(PlayerPositionSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_FOUND);

		optPlayer.get().getPosition().update(request.getX(), request.getY(), request.getZ(), request.getYaw(), request.getPitch());
		if (optPlayer.get().getPosition().getX() != request.getX() || optPlayer.get().getPosition().getY() != request.getY()
				|| optPlayer.get().getPosition().getZ() != request.getZ() || optPlayer.get().getPosition().getYaw() != request.getYaw()
				|| optPlayer.get().getPosition().getPitch() != request.getPitch())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Update the value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to update the value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setParameterValue(ParameterValueSetMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PARAMETER_NOT_FOUND);

		optParameter.get().setValue(request.getNewValue());
		if (optParameter.get().getValue() != request.getNewValue())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, optChannel.get().getName(), optParameter.get().getName(), optParameter.get().getType(), request.getNewValue());
	}

	/**
	 * Update the minimum value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to update the minimum value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setParameterMinValue(ParameterMinValueSetMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PARAMETER_NOT_FOUND);

		if (!(optParameter.get() instanceof IRangeParameter<?>))
			return MumbleServerMessageFactory.answer(request, ErrorCode.PARAMETER_WITHOUT_MIN);

		IRangeParameter<?> range = (IRangeParameter<?>) optParameter.get();
		range.setMin(request.getNewMinValue());
		if (range.getMin() != request.getNewMinValue())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, optChannel.get().getName(), optParameter.get().getName(), optParameter.get().getType(),
				request.getNewMinValue());
	}

	/**
	 * Update the maximum value of a parameter of a sound modifier associated to a a channel.
	 * 
	 * @param request The request sent by the remote in order to update the maximum value of a parameter
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setParameterMaxValue(ParameterMaxValueSetMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<IParameter<?>> optParameter = optChannel.get().getSoundModifier().getParameters().get(request.getParameterName());
		if (!optParameter.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PARAMETER_NOT_FOUND);

		if (!(optParameter.get() instanceof IRangeParameter<?>))
			return MumbleServerMessageFactory.answer(request, ErrorCode.PARAMETER_WITHOUT_MAX);

		IRangeParameter<?> range = (IRangeParameter<?>) optParameter.get();
		range.setMax(request.getNewMaxValue());
		if (range.getMax() != request.getNewMaxValue())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, optChannel.get().getName(), optParameter.get().getName(), optParameter.get().getType(),
				request.getNewMaxValue());
	}

	/**
	 * Set the sound modifier of a channel.
	 * 
	 * @param request The request sent by the remote in order to set the sound modifier of a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setChannelSoundModifier(SoundModifierSetMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().get(request.getChannelInfo().getName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_NOT_FOUND);

		Optional<ISoundModifier> optModifier = SoundManager.getByName(request.getChannelInfo().getSoundModifierInfo().getName());
		if (!optModifier.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

		ParameterList parameterList = new ParameterList();
		for (FullParameterInfo parameterInfo : request.getChannelInfo().getSoundModifierInfo().getParameterInfo().values())
			parameterList.add(parameterInfo);

		optModifier.get().getParameters().update(parameterList);
		optChannel.get().setSoundModifier(optModifier.get());

		if (!optChannel.get().getSoundModifier().equals(optModifier.get()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}
}
