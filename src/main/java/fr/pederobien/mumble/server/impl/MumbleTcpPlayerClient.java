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
import fr.pederobien.mumble.server.event.ParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.PlayerAdminStatusChangeEvent;
import fr.pederobien.mumble.server.event.PlayerDeafenChangeEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.PlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.PlayerMuteChangeEvent;
import fr.pederobien.mumble.server.event.PlayerOnlineChangeEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class MumbleTcpPlayerClient implements IEventListener {
	private InternalServer server;
	private MumblePlayerClient playerClient;
	private MumbleTcpClient tcpClient;
	private boolean isJoined;

	/**
	 * Creates a TCP client associated to a specific player client.
	 * 
	 * @param server       The server attached to this TCP client.
	 * @param playerClient The player client associated to this TCP client.
	 * @param connection   The TCP connection in order to receive/send request to the remote.
	 */
	protected MumbleTcpPlayerClient(InternalServer server, MumblePlayerClient playerClient, ITcpConnection connection) {
		this.server = server;
		this.playerClient = playerClient;

		tcpClient = new MumbleTcpClient(connection);

		EventManager.registerListener(this);
	}

	/**
	 * @return The TCP connection with the remote.
	 */
	public ITcpConnection getConnection() {
		return tcpClient.getConnection();
	}

	@EventHandler
	private void onPlayerAdminChange(PlayerAdminStatusChangeEvent event) {
		if (!event.getPlayer().equals(playerClient.getPlayer()))
			return;

		doIfPlayerJoined(() -> tcpClient.onPlayerAdminChange(event.getPlayer()));
	}

	@EventHandler
	private void onPlayerOnlineChange(PlayerOnlineChangeEvent event) {
		if (!event.getPlayer().equals(playerClient.getPlayer()))
			return;

		doIfPlayerJoined(() -> tcpClient.onPlayerOnlineChange(event.getPlayer()));
	}

	@EventHandler
	private void sendPlayerMuteChanged(PlayerMuteChangeEvent event) {
		doIfPlayerJoined(() -> tcpClient.onPlayerMuteChange(event.getPlayer()));
	}

	@EventHandler
	private void sendPlayerDeafenChanged(PlayerDeafenChangeEvent event) {
		doIfPlayerJoined(() -> tcpClient.onPlayerDeafenChange(event.getPlayer()));
	}

	@EventHandler
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(server))
			return;

		doIfPlayerJoined(() -> tcpClient.onChannelAdd(event.getChannel()));
	}

	@EventHandler
	private void onChannelRemoved(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(server))
			return;

		doIfPlayerJoined(() -> tcpClient.onChannelRemove(event.getChannel()));
	}

	@EventHandler
	private void onChannelRenamed(ChannelNameChangePostEvent event) {
		doIfPlayerJoined(() -> tcpClient.onChannelNameChange(event.getChannel(), event.getOldName()));
	}

	@EventHandler
	private void onPlayerAdded(PlayerListPlayerAddPostEvent event) {
		doIfPlayerJoined(() -> tcpClient.onPlayerAdd(event.getList().getChannel(), event.getPlayer()));
	}

	@EventHandler
	private void onPlayerRemoved(PlayerListPlayerRemovePostEvent event) {
		doIfPlayerJoined(() -> tcpClient.onPlayerRemove(event.getList().getChannel(), event.getPlayer()));
	}

	@EventHandler
	private void onSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		doIfPlayerJoined(() -> tcpClient.onSoundModifierChange(event.getChannel()));
	}

	@EventHandler
	private void onParameterValueChange(ParameterValueChangePostEvent event) {
		doIfPlayerJoined(() -> tcpClient.onParameterValueChange(event.getParameter()));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		if (!event.getConnection().equals(tcpClient.getConnection()))
			return;

		IMumbleMessage request = MumbleServerMessageFactory.parse(event.getAnswer());

		// There is no need to answer to a server join request.
		if (request.getHeader().getIdc() == Idc.SERVER_JOIN) {
			isJoined = true;
			tcpClient.send(MumbleServerMessageFactory.answer(request));
			return;
		}

		// Always allow this request whatever the client state.
		if (request.getHeader().getIdc() == Idc.SERVER_LEAVE) {
			isJoined = false;
			tcpClient.send(MumbleServerMessageFactory.answer(request));
			return;
		}

		if (checkPermission(request))
			tcpClient.send(server.getRequestManager().answer(request));
		else
			tcpClient.send(MumbleServerMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	@EventHandler
	private void OnConnectionLostEvent(ConnectionLostEvent event) {
		if (!event.getConnection().equals(tcpClient.getConnection()))
			return;

		removePlayerFromChannel();

		tcpClient.getConnection().dispose();
		EventManager.callEvent(new ClientDisconnectPostEvent(playerClient));
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		tcpClient.getConnection().dispose();
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
		case PLAYER_INFO:
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

	private void removePlayerFromChannel() {
		if (playerClient.getPlayer() != null && playerClient.getPlayer().getChannel() != null)
			playerClient.getPlayer().getChannel().getPlayers().remove(playerClient.getPlayer());
	}

	private void doIfPlayerJoined(Runnable runnable) {
		if (isJoined)
			runnable.run();
	}
}
