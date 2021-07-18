package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.IObsTcpConnection;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.mumble.server.interfaces.observers.IObsChannel;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;

public class TcpClient implements IObsServer, IObsChannel, IObsTcpConnection {
	private InternalServer internalServer;
	private Client client;
	private ITcpConnection connection;
	private AtomicBoolean isJoined;

	public TcpClient(InternalServer internalServer, Client client, ITcpConnection connection) {
		this.internalServer = internalServer;
		this.client = client;
		this.connection = connection;

		isJoined = new AtomicBoolean(false);
		connection.addObserver(this);
		internalServer.addObserver(this);
		internalServer.getChannels().values().forEach(channel -> channel.addObserver(this));
	}

	@Override
	public void onChannelAdded(IChannel channel) {
		doIfPlayerJoined(() -> {
			channel.addObserver(this);
			send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.ADD, channel.getName()));
		});
	}

	@Override
	public void onChannelRemoved(IChannel channel) {
		doIfPlayerJoined(() -> {
			channel.removeObserver(this);
			send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.REMOVE, channel.getName()));
		});
	}

	@Override
	public void onChannelRenamed(IChannel channel, String oldName, String newName) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.SET, oldName, newName)));
	}

	@Override
	public void onPlayerAdded(IChannel channel, IPlayer player) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.ADD, channel.getName(), player.getName())));
	}

	@Override
	public void onPlayerRemoved(IChannel channel, IPlayer player) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.REMOVE, channel.getName(), player.getName())));
	}

	@Override
	public void onSoundModifierChanged(IChannel channel, ISoundModifier modifier) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.SOUND_MODIFIER, Oid.SET, channel.getName(), modifier.getName())));
	}

	@Override
	public void onServerClosing() {
		connection.dispose();
		internalServer.removeObserver(this);
	}

	@Override
	public void onConnectionComplete() {

	}

	@Override
	public void onConnectionDisposed() {

	}

	@Override
	public void onConnectionLost() {
		connection.removeObserver(this);

		if (client.getPlayer() != null && client.getPlayer().getChannel() != null)
			client.getPlayer().getChannel().removePlayer(client.getPlayer());
		onLeave();
	}

	@Override
	public void onDataReceived(DataReceivedEvent event) {
	}

	@Override
	public void onLog(LogEvent event) {

	}

	@Override
	public void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		IMessage<Header> request = MumbleMessageFactory.parse(event.getAnswer());
		if (checkPermission(request))
			send(internalServer.answer(new RequestEvent(client, request)));
		else
			send(MumbleMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	public InetSocketAddress getAddress() {
		return connection.getAddress();
	}

	public void sendAdminChanged(boolean isAdmin) {
		doIfPlayerJoined(() -> send(MumbleMessageFactory.create(Idc.PLAYER_ADMIN, Oid.SET, isAdmin)));
	}

	public void sendPlayerStatusChanged(boolean isConnected) {
		doIfPlayerJoined(() -> {
			if (isConnected)
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

	public void onJoin() {
		isJoined.set(true);
	}

	public void onLeave() {
		isJoined.set(false);
	}

	private void send(IMessage<Header> message) {
		if (connection == null || connection.isDisposed())
			return;
		connection.send(new MumbleCallbackMessage(message, null));
	}

	private boolean checkPermission(IMessage<Header> request) {
		if (!isJoined.get())
			return request.getHeader().getIdc() == Idc.SERVER_JOIN;

		switch (request.getHeader().getIdc()) {
		case UNIQUE_IDENTIFIER:
		case PLAYER_INFO:
		case UDP_PORT:
		case PLAYER_MUTE:
		case PLAYER_DEAFEN:
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
		default:
			return client.getPlayer() != null && client.getPlayer().isAdmin();
		}
	}

	private void doIfPlayerJoined(Runnable runnable) {
		if (isJoined.get())
			runnable.run();
	}
}
