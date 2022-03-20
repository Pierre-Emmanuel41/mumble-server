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
import fr.pederobien.mumble.common.impl.messages.v10.PlayerAddMessageV10;
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
import fr.pederobien.mumble.common.impl.model.ParameterInfo.LazyParameterInfo;
import fr.pederobien.mumble.common.impl.model.PlayerInfo.FullPlayerInfo;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.exceptions.PlayerNotRegisteredInChannelException;
import fr.pederobien.mumble.server.impl.InternalServer;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
import fr.pederobien.mumble.server.impl.Player;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.impl.modifiers.Parameter;
import fr.pederobien.mumble.server.impl.modifiers.ParameterList;
import fr.pederobien.mumble.server.impl.modifiers.RangeParameter;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPosition;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;

public class RequestServerManagementV10 extends RequestServerManagement {

	/**
	 * Creates a request management in order to modify the given getServer() and answer to remote getRequests().
	 * 
	 * @param server The server to update.
	 */
	public RequestServerManagementV10(InternalServer server) {
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

		// Player mute map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerMuteMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerMuteMap.put(Oid.SET, request -> setPlayerMute((PlayerMuteSetMessageV10) request));
		getRequests().put(Idc.PLAYER_MUTE, playerMuteMap);

		// Channels player map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> channelsPlayerMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		channelsPlayerMap.put(Oid.ADD, request -> channelsPlayerAdd((ChannelsPlayerAddMessageV10) request));
		channelsPlayerMap.put(Oid.SET, request -> channelsPlayerRemove((ChannelsPlayerRemoveMessageV10) request));
		getRequests().put(Idc.CHANNELS_PLAYER, channelsPlayerMap);

		// Player deafen map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerDeafenMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerDeafenMap.put(Oid.SET, request -> playerDeafenSet((PlayerDeafenSetMessageV10) request));
		getRequests().put(Idc.PLAYER_DEAFEN, playerDeafenMap);

		// Player mute by map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerMuteByMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerMuteByMap.put(Oid.SET, request -> playerMuteBySet((PlayerMuteBySetMessageV10) request));
		getRequests().put(Idc.PLAYER_MUTE_BY, playerMuteByMap);

		// Player kick map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerKickMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerKickMap.put(Oid.ADD, request -> playerKickSet((PlayerKickSetMessageV10) request));
		getRequests().put(Idc.PLAYER_KICK, playerKickMap);

		// Player position map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> playerPositionMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		playerPositionMap.put(Oid.GET, request -> playerPositionGet((PlayerPositionGetMessageV10) request));
		playerPositionMap.put(Oid.SET, request -> playerPositionSet((PlayerPositionSetMessageV10) request));
		getRequests().put(Idc.CHANNELS_PLAYER, playerPositionMap);

		// Sound modifier map
		Map<Oid, Function<IMumbleMessage, IMumbleMessage>> soundModifierMap = new HashMap<Oid, Function<IMumbleMessage, IMumbleMessage>>();
		soundModifierMap.put(Oid.GET, request -> soundModifierGet((SoundModifierGetMessageV10) request));
		soundModifierMap.put(Oid.SET, request -> soundModifierSet((SoundModifierSetMessageV10) request));
		soundModifierMap.put(Oid.INFO, request -> soundModifierInfo((SoundModifierInfoMessageV10) request));
		getRequests().put(Idc.CHANNELS_PLAYER, soundModifierMap);
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
	protected IMumbleMessage channelsPlayerAdd(ChannelsPlayerAddMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().getChannel(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_DOES_NOT_EXISTS);

		final Optional<IPlayer> optPlayerAdd = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayerAdd.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		// A player cannot be registered in two channels at the same time.
		if (getServer().getPlayers().getPlayersInChannel().contains(optPlayerAdd.get()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_ALREADY_REGISTERED);

		// Doing modification on the getServer().
		optChannel.get().getPlayers().add(optPlayerAdd.get());
		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	@Override
	protected IMumbleMessage channelsPlayerRemove(ChannelsPlayerRemoveMessageV10 request) {
		Optional<IChannel> optChannel = getServer().getChannels().getChannel(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_DOES_NOT_EXISTS);

		final Optional<IPlayer> optPlayerRemove = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayerRemove.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		// doing modification on the getServer().
		optChannel.get().getPlayers().remove(optPlayerRemove.get());
		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	@Override
	protected IMumbleMessage playerDeafenSet(PlayerDeafenSetMessageV10 request) {
		try {
			Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
			if (!optPlayer.isPresent())
				return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

			optPlayer.get().setDeafen(request.isDeafen());
			return MumbleServerMessageFactory.answer(request, request.getProperties());
		} catch (PlayerNotRegisteredInChannelException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_REGISTERED);
		}
	}

	@Override
	protected IMumbleMessage playerMuteBySet(PlayerMuteBySetMessageV10 request) {
		Optional<IPlayer> optMutingPlayer = getServer().getPlayers().get(request.getMutingPlayer());
		if (!optMutingPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		Optional<IPlayer> optMutedPlayer = getServer().getPlayers().get(request.getMutedPlayer());
		if (!optMutedPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		if (!optMutingPlayer.get().isAdmin() && !optMutedPlayer.get().getChannel().equals(optMutingPlayer.get().getChannel()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYERS_IN_DIFFERENT_CHANNELS);

		((Player) optMutedPlayer.get()).setIsMuteBy(optMutingPlayer.get(), request.isMute());
		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	@Override
	protected IMumbleMessage playerKickSet(PlayerKickSetMessageV10 request) {
		final Optional<IPlayer> optKickedPlayer = getServer().getPlayers().get(request.getKickedPlayer());
		if (!optKickedPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		try {
			optKickedPlayer.get().getChannel().getPlayers().remove(optKickedPlayer.get());
			return MumbleServerMessageFactory.answer(request, request.getProperties());
		} catch (NullPointerException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_REGISTERED);
		}
	}

	@Override
	protected IMumbleMessage playerPositionGet(PlayerPositionGetMessageV10 request) {
		String playerName = request.getPlayerInfo().getName();

		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerInfo().getName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		IPosition position = optPlayer.get().getPosition();
		return MumbleServerMessageFactory.answer(request, playerName, position.getX(), position.getY(), position.getZ(), position.getYaw(), position.getPitch());
	}

	@Override
	protected IMumbleMessage playerPositionSet(PlayerPositionSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		optPlayer.get().getPosition().update(request.getX(), request.getY(), request.getZ(), request.getYaw(), request.getPitch());
		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	@Override
	protected IMumbleMessage soundModifierGet(SoundModifierGetMessageV10 request) {
		List<Object> informations = new ArrayList<Object>();

		// channel's name
		Optional<IChannel> optChannel = getServer().getChannels().getChannel(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_DOES_NOT_EXISTS);

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
	protected IMumbleMessage soundModifierSet(SoundModifierSetMessageV10 request) {
		// Channel's name
		Optional<IChannel> optChannel = getServer().getChannels().getChannel(request.getChannelName());
		if (!optChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_DOES_NOT_EXISTS);

		// Modifier's name
		Optional<ISoundModifier> optModifier = SoundManager.getByName(request.getSoundModifierInfo().getName());
		if (!optModifier.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

		ParameterList parameterList = new ParameterList();
		for (LazyParameterInfo parameterInfo : request.getSoundModifierInfo().getParameterInfo())
			parameterList.add(Parameter.fromType(parameterInfo.getType(), parameterInfo.getName(), parameterInfo.getValue(), parameterInfo.getValue()));

		if (optChannel.get().getSoundModifier().equals(optModifier.get()))
			optChannel.get().getSoundModifier().getParameters().update(parameterList);
		else {
			optModifier.get().getParameters().update(parameterList);
			optChannel.get().setSoundModifier(optModifier.get());
		}

		return MumbleServerMessageFactory.answer(request, request.getProperties());
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

				// Parameter's value
				informations.add(parameter.getValue());
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

				// Parameter's value
				informations.add(parameter.getValue());
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
		if (getServer().getChannels().getChannel(request.getChannelInfo().getName()).isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_ALREADY_EXISTS);

		Optional<ISoundModifier> optModifier = SoundManager.getByName(request.getChannelInfo().getSoundModifierInfo().getName());
		if (!optModifier.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.SOUND_MODIFIER_DOES_NOT_EXIST);

		ParameterList parameterList = new ParameterList();
		for (LazyParameterInfo parameterInfo : request.getChannelInfo().getSoundModifierInfo().getParameterInfo())
			parameterList.add(Parameter.fromType(parameterInfo.getType(), parameterInfo.getName(), parameterInfo.getValue(), parameterInfo.getValue()));

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
		Optional<IChannel> optChannel = getServer().getChannels().getChannel(request.getChannelName());
		if (optChannel.isPresent()) {
			if (getServer().getChannels().remove(request.getChannelName()) == null)
				return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

			return MumbleServerMessageFactory.answer(request, request.getProperties());
		} else
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_DOES_NOT_EXISTS);
	}

	/**
	 * Renames a channel based on the properties of the given request.
	 * 
	 * @param request The request sent by the remote in order to rename a channel.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage renameChannel(ChannelsSetMessageV10 request) {
		Optional<IChannel> optOldChannel = getServer().getChannels().getChannel(request.getOldName());
		if (!optOldChannel.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.CHANNEL_DOES_NOT_EXISTS);

		Optional<IChannel> optNewChannel = getServer().getChannels().getChannel(request.getNewName());
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
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

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
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

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
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

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
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

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
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

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
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		optPlayer.get().setGameAddress(request.getGameAddress());
		if (!optPlayer.get().getGameAddress().equals(request.getGameAddress()))
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}

	/**
	 * Update the mute status of a player.
	 * 
	 * @param request The request received from the remote in order to update de mute status of a player.
	 * 
	 * @return The server answer.
	 */
	private IMumbleMessage setPlayerMute(PlayerMuteSetMessageV10 request) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(request.getPlayerName());
		if (!optPlayer.isPresent())
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_RECOGNIZED);

		try {
			optPlayer.get().setMute(request.isMute());
		} catch (PlayerNotRegisteredInChannelException e) {
			return MumbleServerMessageFactory.answer(request, ErrorCode.PLAYER_NOT_REGISTERED);
		}

		if (optPlayer.get().isMute() != request.isMute())
			return MumbleServerMessageFactory.answer(request, ErrorCode.REQUEST_CANCELLED);

		return MumbleServerMessageFactory.answer(request, request.getProperties());
	}
}
