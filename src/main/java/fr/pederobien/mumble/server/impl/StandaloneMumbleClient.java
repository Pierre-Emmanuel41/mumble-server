package fr.pederobien.mumble.server.impl;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Identifier;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterMaxValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterMinValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerGameAddressChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerKickPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteByChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerPositionChangePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.LogEvent;

public class StandaloneMumbleClient extends AbstractMumbleConnection implements IEventListener {

	/**
	 * Creates a client associated to the external game server.
	 * 
	 * @param server     The server associated to this client.
	 * @param connection The connection with the external game server in order to send/receive data.
	 */
	protected StandaloneMumbleClient(StandaloneMumbleServer server, ITcpConnection connection) {
		super(server, connection);

		if (establishCommunicationProtocolVersion())
			EventManager.registerListener(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelAdd(getVersion(), event.getChannel()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelRemove(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelRemove(getVersion(), event.getChannel()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelNameChange(ChannelNameChangePostEvent event) {
		if (!event.getChannel().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelNameChange(getVersion(), event.getChannel(), event.getOldName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerAdd(ServerPlayerAddPostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onServerPlayerAdd(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerRemove(ServerPlayerRemovePostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onServerPlayerRemove(getVersion(), event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerNameChange(getVersion(), event.getOldName(), event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerOnlineChange(PlayerOnlineChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerOnlineChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerGameAddressChange(PlayerGameAddressChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerGameAddressChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerAdminChange(PlayerAdminChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerAdminChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteChange(PlayerMuteChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerMuteChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteByChange(PlayerMuteByChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerMuteByChange(getVersion(), event.getPlayer(), event.getSource()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeafenChange(PlayerDeafenChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerDeafenChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerKick(PlayerKickPostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerKick(getVersion(), event.getPlayer(), event.getKickingPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerPositionChange(PlayerPositionChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onPlayerPositionChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerAdd(PlayerListPlayerAddPostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onChannelPlayerAdd(getVersion(), event.getList().getChannel(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerRemove(PlayerListPlayerRemovePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		send(getServer().getRequestManager().onChannelPlayerRemove(getVersion(), event.getList().getChannel(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterValueChange(ParameterValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(getServer().getRequestManager().onParameterValueChange(getVersion(), event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMinValueChange(ParameterMinValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(getServer().getRequestManager().onParameterMinValueChange(getVersion(), event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMaxValueChange(ParameterMaxValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(getServer().getRequestManager().onParameterMaxValueChange(getVersion(), event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getChannel()))
			return;

		send(getServer().getRequestManager().onChannelSoundModifierChange(getVersion(), event.getChannel()));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		IMumbleMessage request = checkReceivedRequest(event);
		if (request == null)
			return;

		if (getVersion() != -1 && getVersion() != request.getHeader().getVersion()) {
			String format = "Receiving message with unexpected getVersion() of the communication protocol, expected=v%s, actual=v%s";
			EventManager.callEvent(new LogEvent(format, getVersion(), request.getHeader().getVersion()));
		} else {
			if (request.getHeader().getIdentifier() != Identifier.UNKNOWN)
				send(getServer().getRequestManager().answer(new RequestReceivedHolder(request, this)));
			else
				send(MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
		}
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (!event.getConnection().equals(getTcpConnection()))
			return;

		getTcpConnection().dispose();
		getServer().getPlayers().clear();
		EventManager.unregisterListener(this);
	}
}
