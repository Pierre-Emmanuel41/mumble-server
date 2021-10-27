package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.ChannelPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ClientDisconnectPostEvent;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class TcpClient implements IEventListener {
	private InternalServer internalServer;
	private Client client;
	private ITcpConnection connection;
	private AtomicBoolean isJoined;

	public TcpClient(InternalServer internalServer, Client client, ITcpConnection connection) {
		this.internalServer = internalServer;
		this.client = client;
		this.connection = connection;

		isJoined = new AtomicBoolean(false);
		EventManager.registerListener(this);
	}

	public InetSocketAddress getAddress() {
		return connection.getAddress();
	}

	public void sendAdminChanged(boolean isAdmin) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.PLAYER_ADMIN, Oid.SET, isAdmin)));
	}

	public void sendOnlineChanged(boolean isOnline) {
		doIfPlayerJoined(() -> {
			if (isOnline)
				send(MumbleMessageFactory.create(Idc.PLAYER_INFO, true, client.getPlayer().getName(), client.getPlayer().isAdmin()));
			else {
				send(MumbleMessageFactory.create(Idc.PLAYER_INFO, false));
				if (client.getPlayer().getChannel() != null)
					client.getPlayer().getChannel().removePlayer(client.getPlayer());
			}
		});
	}

	public void sendPlayerMuteChanged(String playerName, boolean isMute) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.PLAYER_MUTE, Oid.SET, playerName, isMute)));
	}

	public void sendPlayerDeafenChanged(String playerName, boolean isDeafen) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.PLAYER_DEAFEN, Oid.SET, playerName, isDeafen)));
	}

	/**
	 * Specify that the mumble client has joined the server.
	 */
	public void onJoin() {
		isJoined.set(true);
	}

	/**
	 * Specify that the mumble client has left the server.
	 */
	public void onLeave() {
		isJoined.set(false);
		removePlayerFromChannel();
	}

	/**
	 * @return The TCP connection with the mumble client.
	 */
	public ITcpConnection getConnection() {
		return connection;
	}

	@EventHandler
	private void onChannelAdded(ServerChannelAddPostEvent event) {
		if (!event.getServer().equals(internalServer.getMumbleServer()))
			return;

		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.ADD, event.getChannel().getName(), event.getChannel().getSoundModifier().getName())));
	}

	@EventHandler
	private void onChannelRemoved(ServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(internalServer.getMumbleServer()))
			return;

		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.REMOVE, event.getChannel().getName())));
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		connection.dispose();
		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onChannelRenamed(ChannelNameChangePostEvent event) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.SET, event.getOldName(), event.getChannel().getName())));
	}

	@EventHandler
	private void onPlayerAdded(ChannelPlayerAddPostEvent event) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.ADD, event.getChannel().getName(), event.getPlayer().getName())));
	}

	@EventHandler
	private void onPlayerRemoved(ChannelPlayerRemovePostEvent event) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.REMOVE, event.getChannel().getName(), event.getPlayer().getName())));
	}

	@EventHandler
	private void onSoundModifierChanged(ChannelSoundModifierChangePostEvent event) {
		doIfPlayerJoined(
				() -> send(MumbleMessageFactory.create(Idc.SOUND_MODIFIER, Oid.SET, event.getChannel().getName(), event.getChannel().getSoundModifier().getName())));
	}

	@EventHandler
	private void OnConnectionLostEvent(ConnectionLostEvent event) {
		if (!event.getConnection().equals(connection))
			return;

		removePlayerFromChannel();

		connection.dispose();
		EventManager.callEvent(new ClientDisconnectPostEvent(client));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		if (!event.getConnection().equals(connection))
			return;

		IMessage<Header> request = MumbleMessageFactory.parse(event.getAnswer());
		if (checkPermission(request))
			send(internalServer.answer(new RequestEvent(client, request)));
		else
			send(MumbleMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	private void send(IMessage<Header> message) {
		if (connection == null || connection.isDisposed())
			return;
		connection.send(new MumbleCallbackMessage(message, null));
	}

	private void removePlayerFromChannel() {
		if (client.getPlayer() != null && client.getPlayer().getChannel() != null)
			client.getPlayer().getChannel().removePlayer(client.getPlayer());
	}

	private boolean checkPermission(IMessage<Header> request) {
		if (!isJoined.get())
			return request.getHeader().getIdc() == Idc.SERVER_JOIN;

		switch (request.getHeader().getIdc()) {
		case PLAYER_INFO:
		case PLAYER_MUTE:
		case PLAYER_DEAFEN:
		case SERVER_LEAVE:
		case GAME_PORT:
			return true;
		case CHANNELS:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			case ADD:
			case REMOVE:
				return client.getPlayer() != null && client.getPlayer().isAdmin();
			default:
				return true;
			}
		case CHANNELS_PLAYER:
			switch (request.getHeader().getOid()) {
			case ADD:
			case REMOVE:
				return client.getPlayer() != null && client.getPlayer().isOnline();
			default:
				return false;
			}
		case SOUND_MODIFIER:
			switch (request.getHeader().getOid()) {
			case GET:
			case INFO:
				return true;
			case SET:
				return client.getPlayer() != null && client.getPlayer().isAdmin();
			default:
				return false;
			}
		default:
			return client.getPlayer() != null && client.getPlayer().isAdmin();
		}
	}

	private void doIfPlayerJoined(Runnable runnable) {
		if (isJoined.get())
			runnable.run();
	}
}
