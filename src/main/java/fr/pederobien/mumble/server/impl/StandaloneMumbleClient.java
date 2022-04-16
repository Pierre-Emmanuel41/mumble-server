package fr.pederobien.mumble.server.impl;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.Oid;
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

public class StandaloneMumbleClient implements IEventListener {
	private ITcpConnection tcpConnection;
	private MumbleTcpClient tcpClient;
	private InternalServer server;

	/**
	 * Creates a client associated to the external game server.
	 * 
	 * @param server        The server associated to this client.
	 * @param tcpConnection The connection with the external game server in order to send/receive data.
	 */
	protected StandaloneMumbleClient(InternalServer internalServer, ITcpConnection tcpConnection) {
		this.server = internalServer;
		this.tcpConnection = tcpConnection;

		tcpClient = new MumbleTcpClient(tcpConnection);

		EventManager.registerListener(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(server))
			return;

		tcpClient.onChannelAdd(event.getChannel());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelRemoved(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(server))
			return;

		tcpClient.onChannelRemove(event.getChannel());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelNameChange(ChannelNameChangePostEvent event) {
		if (!event.getChannel().getServer().equals(server))
			return;

		tcpClient.onChannelNameChange(event.getChannel(), event.getOldName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerAdd(ServerPlayerAddPostEvent event) {
		if (!event.getList().getServer().equals(server))
			return;

		tcpClient.onServerPlayerAdd(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerRemove(ServerPlayerRemovePostEvent event) {
		if (!event.getList().getServer().equals(server))
			return;

		tcpClient.onServerPlayerRemove(event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerNameChange(event.getOldName(), event.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerOnlineChange(PlayerOnlineChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerOnlineChange(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerGameAddressChange(PlayerGameAddressChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerGameAddressChange(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerAdminChange(PlayerAdminChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerAdminChange(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteChange(PlayerMuteChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerMuteChange(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteByChange(PlayerMuteByChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerMuteByChange(event.getPlayer(), event.getSource());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeafenChange(PlayerDeafenChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerDeafenChange(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerKick(PlayerKickPostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerKick(event.getPlayer(), event.getKickingPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerPositionChange(PlayerPositionChangePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onPlayerPositionChange(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerAdd(PlayerListPlayerAddPostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onChannelPlayerAdd(event.getList().getChannel(), event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerRemove(PlayerListPlayerRemovePostEvent event) {
		if (!server.getPlayers().toList().contains(event.getPlayer()))
			return;

		tcpClient.onChannelPlayerRemove(event.getList().getChannel(), event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterValueChange(ParameterValueChangePostEvent event) {
		if (!server.getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		tcpClient.onParameterValueChange(event.getParameter());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMinValueChange(ParameterMinValueChangePostEvent event) {
		if (!server.getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		tcpClient.onParameterMinValueChange(event.getParameter());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMaxValueChange(ParameterMaxValueChangePostEvent event) {
		if (!server.getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		tcpClient.onParameterMaxValueChange(event.getParameter());
	}

	@EventHandler
	private void onSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		send(MumbleServerMessageFactory.create(Idc.SOUND_MODIFIER, Oid.SET, event.getChannel().getName(), event.getChannel().getSoundModifier().getName()));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		if (!event.getConnection().equals(tcpConnection))
			return;

		IMumbleMessage request = MumbleServerMessageFactory.parse(event.getAnswer());

		if (checkPermission(request))
			send(server.getRequestManager().answer(request));
		else
			send(MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (!event.getConnection().equals(tcpConnection))
			return;

		tcpConnection.dispose();
		EventManager.unregisterListener(this);
	}

	private void send(IMumbleMessage message) {
		if (tcpConnection.isDisposed())
			return;

		tcpConnection.send(new MumbleCallbackMessage(message, null));
	}

	private boolean checkPermission(IMumbleMessage request) {
		switch (request.getHeader().getIdc()) {
		case SERVER_INFO:
		case CHANNELS:
		case PLAYER:
		case PLAYER_NAME:
		case PLAYER_ONLINE:
		case PLAYER_GAME_ADDRESS:
		case PLAYER_ADMIN:
		case PLAYER_MUTE:
		case PLAYER_DEAFEN:
		case PLAYER_MUTE_BY:
		case CHANNELS_PLAYER:
		case PARAMETER_VALUE:
		case PARAMETER_MIN_VALUE:
		case PARAMETER_MAX_VALUE:
		case SOUND_MODIFIER:
		case PLAYER_KICK:
		case PLAYER_POSITION:
			return true;
		default:
			return false;
		}
	}
}
