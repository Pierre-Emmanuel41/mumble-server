package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.impl.TcpServerConnection;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.communication.interfaces.IObsTcpConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.MumbleRequestMessage;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.observers.IObsChannel;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;

public class TcpClient implements IObsServer, IObsChannel, IObsTcpConnection {
	private InternalServer internalServer;
	private ITcpConnection serverConnection;
	private Player player;
	private UUID uuid;
	private InetSocketAddress address;
	private Channel channel;

	protected TcpClient(InternalServer internalServer, UUID uuid, InetSocketAddress address) {
		this.internalServer = internalServer;
		this.uuid = uuid;
		this.address = address;
		internalServer.addObserver(this);
		internalServer.getChannels().forEach(channel -> channel.addObserver(this));
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
		if (serverConnection != null)
			serverConnection.dispose();
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
		serverConnection.removeObserver(this);
		if (channel != null)
			channel.removePlayer(getPlayer());
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
			send(internalServer.answer(new RequestEvent(this, request)));
		else
			send(MumbleMessageFactory.answer(request, ErrorCode.PERMISSION_REFUSED));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof TcpClient))
			return false;

		TcpClient other = (TcpClient) obj;
		return uuid.equals(other.getUUID()) || getAddress().equals(other.getAddress());
	}

	public void createTcpConnection(Socket socket) {
		this.serverConnection = new TcpServerConnection(socket, new MessageExtractor());
		serverConnection.addObserver(this);
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
		player.setClient(this);
	}

	public UUID getUUID() {
		return uuid;
	}

	public InetSocketAddress getAddress() {
		return serverConnection == null ? address : serverConnection.getAddress();
	}

	public void sendAdminChanged(boolean isAdmin) {
		send(MumbleMessageFactory.create(Idc.PLAYER_ADMIN, isAdmin));
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void sendPlayerStatusChanged(boolean isConnected) {
		if (isConnected)
			send(MumbleMessageFactory.create(Idc.PLAYER_STATUS, true, getPlayer().getName(), getPlayer().isAdmin()));
		else {
			send(MumbleMessageFactory.create(Idc.PLAYER_STATUS, false));
			if (channel != null)
				channel.removePlayer(player);
		}
	}

	private void send(IMessage<Header> message) {
		if (serverConnection == null || serverConnection.isDisposed())
			return;
		serverConnection.send(new MumbleRequestMessage(message, null));
	}

	private boolean checkPermission(IMessage<Header> request) {
		Idc idc = request.getHeader().getIdc();
		switch (idc) {
		case UNIQUE_IDENTIFIER:
		case PLAYER_STATUS:
			return true;
		case CHANNELS:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			case ADD:
			case REMOVE:
				return player != null && player.isAdmin();
			default:
				return true;
			}
		case SERVER_CONFIGURATION:
			switch (request.getHeader().getOid()) {
			case GET:
				return true;
			case SET:
				return player != null && player.isAdmin();
			default:
				return true;
			}
		case CHANNELS_PLAYER:
			switch (request.getHeader().getOid()) {
			case ADD:
			case REMOVE:
				return player != null && player.isOnline();
			default:
				return false;
			}
		default:
			return player != null && player.isAdmin();
		}
	}
}