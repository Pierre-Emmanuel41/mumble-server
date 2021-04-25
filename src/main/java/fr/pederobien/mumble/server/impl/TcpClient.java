package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;

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
import fr.pederobien.mumble.server.interfaces.observers.IObsChannel;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;

public class TcpClient implements IObsServer, IObsChannel, IObsTcpConnection {
	private InternalServer internalServer;
	private Client client;
	private ITcpConnection connection;

	public TcpClient(InternalServer internalServer, Client client, ITcpConnection connection) {
		this.internalServer = internalServer;
		this.client = client;
		this.connection = connection;

		connection.addObserver(this);
		internalServer.addObserver(this);
		internalServer.getChannels().values().forEach(channel -> channel.addObserver(this));
	}

	@Override
	public void onChannelAdded(IChannel channel) {
		channel.addObserver(this);
		send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.ADD, channel.getName()));
	}

	@Override
	public void onChannelRemoved(IChannel channel) {
		channel.removeObserver(this);
		send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.REMOVE, channel.getName()));
	}

	@Override
	public void onChannelRenamed(IChannel channel, String oldName, String newName) {
		send(MumbleMessageFactory.create(Idc.CHANNELS, Oid.SET, oldName, newName));
	}

	@Override
	public void onPlayerAdded(IChannel channel, IPlayer player) {
		send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.ADD, channel.getName(), player.getName()));
	}

	@Override
	public void onPlayerRemoved(IChannel channel, IPlayer player) {
		send(MumbleMessageFactory.create(Idc.CHANNELS_PLAYER, Oid.REMOVE, channel.getName(), player.getName()));
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
		if (client.getChannel() != null)
			client.getChannel().removePlayer(client.getPlayer());
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
		send(MumbleMessageFactory.create(Idc.PLAYER_ADMIN, isAdmin));
	}

	public void sendPlayerStatusChanged(boolean isConnected) {
		if (isConnected)
			send(MumbleMessageFactory.create(Idc.PLAYER_STATUS, true, client.getPlayer().getName(), client.getPlayer().isAdmin()));
		else {
			send(MumbleMessageFactory.create(Idc.PLAYER_STATUS, false));
			if (client.getChannel() != null)
				client.getChannel().removePlayer(client.getPlayer());
		}
	}

	public void sendPlayerMuteChanged(String playerName, boolean isMute) {
		send(MumbleMessageFactory.create(Idc.PLAYER_MUTE, Oid.SET, playerName, isMute));
	}

	private void send(IMessage<Header> message) {
		if (connection == null || connection.isDisposed())
			return;
		connection.send(new MumbleCallbackMessage(message, null));
	}

	private boolean checkPermission(IMessage<Header> request) {
		Idc idc = request.getHeader().getIdc();
		switch (idc) {
		case UNIQUE_IDENTIFIER:
		case PLAYER_STATUS:
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
		case SERVER_CONFIGURATION:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			case SET:
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
}
