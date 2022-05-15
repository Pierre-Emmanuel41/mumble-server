package fr.pederobien.mumble.server.impl.request;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

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
	private NavigableMap<Float, IRequestManager> managers;

	/**
	 * Creates a request management in order to modify the given server and answer to remote requests.
	 * 
	 * @param server The server to update.
	 */
	public ServerRequestManager(IMumbleServer server) {
		managers = new TreeMap<Float, IRequestManager>();
		register(new RequestManagerV10(server));
	}

	@Override
	public float getVersion() {
		return managers.lastKey();
	}

	@Override
	public boolean isSupported(float version) {
		return managers.containsKey(version);
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
	public IMumbleMessage getCommunicationProtocolVersion() {
		return findManagerAndApply(1.0f, manager -> manager.getCommunicationProtocolVersion());
	}

	@Override
	public IMumbleMessage setCommunicationProtocolVersion(float version) {
		return findManagerAndApply(1.0f, manager -> manager.setCommunicationProtocolVersion(version));
	}

	@Override
	public IMumbleMessage onChannelAdd(float version, IChannel channel) {
		return findManagerAndApply(version, manager -> manager.onChannelAdd(channel));
	}

	@Override
	public IMumbleMessage onChannelRemove(float version, IChannel channel) {
		return findManagerAndApply(version, manager -> manager.onChannelRemove(channel));
	}

	@Override
	public IMumbleMessage onChannelNameChange(float version, IChannel channel, String oldName) {
		return findManagerAndApply(version, manager -> manager.onChannelNameChange(channel, oldName));
	}

	@Override
	public IMumbleMessage onServerPlayerAdd(float version, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onServerPlayerAdd(player));
	}

	@Override
	public IMumbleMessage onServerPlayerRemove(float version, String name) {
		return findManagerAndApply(version, manager -> manager.onServerPlayerRemove(name));
	}

	@Override
	public IMumbleMessage onPlayerNameChange(float version, String oldName, String newName) {
		return findManagerAndApply(version, manager -> manager.onPlayerNameChange(oldName, newName));
	}

	@Override
	public IMumbleMessage onPlayerOnlineChange(float version, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onPlayerOnlineChange(player));
	}

	@Override
	public IMumbleMessage onPlayerGameAddressChange(float version, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onPlayerGameAddressChange(player));
	}

	@Override
	public IMumbleMessage onPlayerAdminChange(float version, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onPlayerAdminChange(player));
	}

	@Override
	public IMumbleMessage onPlayerMuteChange(float version, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onPlayerMuteChange(player));
	}

	@Override
	public IMumbleMessage onPlayerMuteByChange(float version, IPlayer target, IPlayer source) {
		return findManagerAndApply(version, manager -> manager.onPlayerMuteByChange(target, source));
	}

	@Override
	public IMumbleMessage onPlayerDeafenChange(float version, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onPlayerDeafenChange(player));
	}

	@Override
	public IMumbleMessage onPlayerKick(float version, IPlayer kicked, IPlayer kicking) {
		return findManagerAndApply(version, manager -> manager.onPlayerKick(kicked, kicking));
	}

	@Override
	public IMumbleMessage onPlayerPositionChange(float version, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onPlayerPositionChange(player));
	}

	@Override
	public IMumbleMessage onChannelPlayerAdd(float version, IChannel channel, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onChannelPlayerAdd(channel, player));
	}

	@Override
	public IMumbleMessage onChannelPlayerRemove(float version, IChannel channel, IPlayer player) {
		return findManagerAndApply(version, manager -> manager.onChannelPlayerRemove(channel, player));
	}

	@Override
	public IMumbleMessage onParameterValueChange(float version, IParameter<?> parameter) {
		return findManagerAndApply(version, manager -> manager.onParameterValueChange(parameter));
	}

	@Override
	public IMumbleMessage onParameterMinValueChange(float version, IRangeParameter<?> parameter) {
		return findManagerAndApply(version, manager -> manager.onParameterMinValueChange(parameter));
	}

	@Override
	public IMumbleMessage onParameterMaxValueChange(float version, IRangeParameter<?> parameter) {
		return findManagerAndApply(version, manager -> manager.onParameterMaxValueChange(parameter));
	}

	@Override
	public IMumbleMessage onChannelSoundModifierChange(float version, IChannel channel) {
		return findManagerAndApply(version, manager -> manager.onChannelSoundModifierChange(channel));
	}

	@Override
	public IMumbleMessage onGamePortCheck(float version, int port) {
		return findManagerAndApply(version, manager -> manager.onGamePortCheck(port));
	}

	private void register(IRequestManager manager) {
		managers.put(manager.getVersion(), manager);
	}

	/**
	 * Apply the function of the manager associated to the given version if registered.
	 * 
	 * @param version  The version of the manager.
	 * @param function The function to apply.
	 * 
	 * @return The created message.
	 */
	private IMumbleMessage findManagerAndApply(float version, Function<IRequestManager, IMumbleMessage> function) {
		IRequestManager manager = managers.get(version);
		if (manager == null)
			return null;

		return function.apply(manager);
	}
}
