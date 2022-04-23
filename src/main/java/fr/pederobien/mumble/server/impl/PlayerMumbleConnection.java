package fr.pederobien.mumble.server.impl;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.ParameterMaxValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterMinValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerPositionChangePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class PlayerMumbleConnection extends AbstractMumbleConnection implements IEventListener {
	private PlayerMumbleClient playerClient;
	private boolean isJoined;

	/**
	 * Creates a mumble connection in order to send or receive requests from the remote. This connection is associated to a player in
	 * order to allow or refuse some requests according to the player administrator status.
	 * 
	 * @param server       The server associated to this connection.
	 * @param playerClient The player client associated to this TCP client.
	 * @param connection   The TCP connection with the remote.
	 */
	protected PlayerMumbleConnection(IMumbleServer server, PlayerMumbleClient playerClient, ITcpConnection connection) {
		super(server, connection);
		this.playerClient = playerClient;

		// Connection disposed if and only if the remote did not answer to the server about the getVersion() of the communication protocol
		// to use.
		if (!connection.isDisposed())
			EventManager.registerListener(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelAdd(getVersion(), event.getChannel())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelRemove(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelRemove(getVersion(), event.getChannel())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelNameChange(ChannelNameChangePostEvent event) {
		if (!event.getChannel().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelNameChange(getVersion(), event.getChannel(), event.getOldName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerAdd(ServerPlayerAddPostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onServerPlayerAdd(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerRemove(ServerPlayerRemovePostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onServerPlayerRemove(getVersion(), event.getPlayer().getName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerNameChange(PlayerNameChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerNameChange(getVersion(), event.getOldName(), event.getPlayer().getName())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerOnlineChange(PlayerOnlineChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerOnlineChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerAdminChange(PlayerAdminChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerAdminChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteChange(PlayerMuteChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerMuteChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeafenChange(PlayerDeafenChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerDeafenChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerPositionChange(PlayerPositionChangePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()) && !event.getPlayer().equals(playerClient.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onPlayerPositionChange(getVersion(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerAdd(PlayerListPlayerAddPostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelPlayerAdd(getVersion(), event.getList().getChannel(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerRemove(PlayerListPlayerRemovePostEvent event) {
		if (!getServer().getPlayers().toList().contains(event.getPlayer()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelPlayerRemove(getVersion(), event.getList().getChannel(), event.getPlayer())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterValueChange(ParameterValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMinValueChange(ParameterMinValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterMinValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMaxValueChange(ParameterMaxValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onParameterMaxValueChange(getVersion(), event.getParameter())));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getChannel()))
			return;

		doIfPlayerJoined(() -> send(getServer().getRequestManager().onChannelSoundModifierChange(getVersion(), event.getChannel())));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		if (!event.getConnection().equals(getTcpConnection()))
			return;

		IMumbleMessage request = MumbleServerMessageFactory.parse(event.getAnswer());

		// There is no need to answer to a server join request.
		if (request.getHeader().getIdc() == Idc.SERVER_JOIN) {
			isJoined = true;
			send(MumbleServerMessageFactory.answer(request));
			return;
		}

		// Always allow this request whatever the client state.
		if (request.getHeader().getIdc() == Idc.SERVER_LEAVE) {
			isJoined = false;
			send(MumbleServerMessageFactory.answer(request));
			return;
		}

		if (checkPermission(request))
			send(getServer().getRequestManager().answer(request));
		else
			send(MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	@EventHandler
	private void OnConnectionLostEvent(ConnectionLostEvent event) {
		if (!event.getConnection().equals(getTcpConnection()))
			return;

		if (playerClient.getPlayer() != null && playerClient.getPlayer().getChannel() != null)
			playerClient.getPlayer().getChannel().getPlayers().remove(playerClient.getPlayer());

		getTcpConnection().dispose();
		EventManager.callEvent(new ClientDisconnectPostEvent(playerClient));
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		getTcpConnection().dispose();
		EventManager.unregisterListener(this);
	}

	private boolean checkPermission(IMumbleMessage request) {
		// No need to check the permission for this Idc.
		if (request.getHeader().getIdc() == Idc.GAME_PORT)
			return true;

		if (!isJoined)
			return false;

		switch (request.getHeader().getIdc()) {
		case SERVER_INFO:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			default:
				return false;
			}
		case PLAYER:
		case PLAYER_MUTE:
		case PLAYER_DEAFEN:
			return true;
		case CHANNELS:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			case ADD:
			case REMOVE:
				return playerClient.getPlayer() != null && playerClient.getPlayer().isAdmin();
			default:
				return false;
			}
		case CHANNELS_PLAYER:
			switch (request.getHeader().getOid()) {
			case ADD:
			case REMOVE:
				return playerClient.getPlayer() != null && playerClient.getPlayer().isOnline();
			default:
				return false;
			}
		case SOUND_MODIFIER:
			switch (request.getHeader().getOid()) {
			case GET:
			case INFO:
				return true;
			case SET:
				return playerClient.getPlayer() != null && playerClient.getPlayer().isAdmin();
			default:
				return false;
			}
		default:
			return playerClient.getPlayer() != null && playerClient.getPlayer().isAdmin();
		}
	}

	private void doIfPlayerJoined(Runnable runnable) {
		if (isJoined)
			runnable.run();
	}
}
