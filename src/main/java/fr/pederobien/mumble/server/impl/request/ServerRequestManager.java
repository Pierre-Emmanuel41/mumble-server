package fr.pederobien.mumble.server.impl.request;

import java.util.HashMap;
import java.util.Map;

import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.impl.MumbleServerMessageFactory;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IRangeParameter;
import fr.pederobien.mumble.server.interfaces.IRequestManager;
import fr.pederobien.mumble.server.interfaces.IServerRequestManager;

public class ServerRequestManager implements IServerRequestManager {
	private Map<Float, IRequestManager> managers;

	/**
	 * Creates a request management in order to modify the given server and answer to remote requests.
	 * 
	 * @param server The server to update.
	 */
	public ServerRequestManager(IMumbleServer server) {
		managers = new HashMap<Float, IRequestManager>();
		managers.put(1.0f, new RequestManagerV10(server));
	}

	/**
	 * run a specific treatment associated to the given request.
	 * 
	 * @param request The request sent by the remote.
	 * 
	 * @return The server response.
	 */
	@Override
	public IMumbleMessage answer(IMumbleMessage request) {
		IRequestManager manager = managers.get(request.getHeader().getVersion());

		if (manager == null)
			return MumbleServerMessageFactory.answer(request, ErrorCode.INCOMPATIBLE_VERSION);

		return manager.answer(request);
	}

	@Override
	public IMumbleMessage onChannelAdd(float version, IChannel channel) {
		return managers.get(version).onChannelAdd(channel);
	}

	@Override
	public IMumbleMessage onChannelRemove(float version, IChannel channel) {
		return managers.get(version).onChannelRemove(channel);
	}

	@Override
	public IMumbleMessage onChannelNameChange(float version, IChannel channel, String oldName) {
		return managers.get(version).onChannelNameChange(channel, oldName);
	}

	@Override
	public IMumbleMessage onServerPlayerAdd(float version, IPlayer player) {
		return managers.get(version).onServerPlayerAdd(player);
	}

	@Override
	public IMumbleMessage onServerPlayerRemove(float version, String name) {
		return managers.get(version).onServerPlayerRemove(name);
	}

	@Override
	public IMumbleMessage onPlayerNameChange(float version, String oldName, String newName) {
		return managers.get(version).onPlayerNameChange(oldName, newName);
	}

	@Override
	public IMumbleMessage onPlayerOnlineChange(float version, IPlayer player) {
		return managers.get(version).onPlayerOnlineChange(player);
	}

	@Override
	public IMumbleMessage onPlayerGameAddressChange(float version, IPlayer player) {
		return managers.get(version).onPlayerGameAddressChange(player);
	}

	@Override
	public IMumbleMessage onPlayerAdminChange(float version, IPlayer player) {
		return managers.get(version).onPlayerAdminChange(player);
	}

	@Override
	public IMumbleMessage onPlayerMuteChange(float version, IPlayer player) {
		return managers.get(version).onPlayerMuteChange(player);
	}

	@Override
	public IMumbleMessage onPlayerMuteByChange(float version, IPlayer target, IPlayer source) {
		return managers.get(version).onPlayerMuteByChange(target, source);
	}

	@Override
	public IMumbleMessage onPlayerDeafenChange(float version, IPlayer player) {
		return managers.get(version).onPlayerDeafenChange(player);
	}

	@Override
	public IMumbleMessage onPlayerKick(float version, IPlayer kicked, IPlayer kicking) {
		return managers.get(version).onPlayerKick(kicked, kicking);
	}

	@Override
	public IMumbleMessage onPlayerPositionChange(float version, IPlayer player) {
		return managers.get(version).onPlayerPositionChange(player);
	}

	@Override
	public IMumbleMessage onChannelPlayerAdd(float version, IChannel channel, IPlayer player) {
		return managers.get(version).onChannelPlayerAdd(channel, player);
	}

	@Override
	public IMumbleMessage onChannelPlayerRemove(float version, IChannel channel, IPlayer player) {
		return managers.get(version).onChannelPlayerRemove(channel, player);
	}

	@Override
	public IMumbleMessage onParameterValueChange(float version, IParameter<?> parameter) {
		return managers.get(version).onParameterValueChange(parameter);
	}

	@Override
	public IMumbleMessage onParameterMinValueChange(float version, IRangeParameter<?> parameter) {
		return managers.get(version).onParameterMinValueChange(parameter);
	}

	@Override
	public IMumbleMessage onParameterMaxValueChange(float version, IRangeParameter<?> parameter) {
		return managers.get(version).onParameterMaxValueChange(parameter);
	}

	@Override
	public IMumbleMessage onChannelSoundModifierChange(float version, IChannel channel) {
		return managers.get(version).onChannelSoundModifierChange(channel);
	}
}
