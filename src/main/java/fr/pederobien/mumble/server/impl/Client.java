package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.IConnection;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.ErrorCode;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.MumbleRequestMessage;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.observers.IObsChannel;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;

public class Client implements IObsServer, IObsChannel, IObsConnection {
	private Map<UUID, Client> clients;
	private InternalServer internalServer;
	private IConnection serverConnection;
	private Player player;
	private UUID uuid;
	private InetAddress address;
	private Channel channel;

	protected Client(InternalServer internalServer, Map<UUID, Client> clients) {
		this.internalServer = internalServer;
		this.clients = clients;
		internalServer.addObserver(this);
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
		System.out.println("Connection lost with the client");
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

		if (!(obj instanceof Client))
			return false;

		Client other = (Client) obj;
		return uuid.equals(other.getUUID()) || getAddress().equals(other.getAddress());
	}

	public void setConnection(IConnection serverConnection) {
		this.serverConnection = serverConnection;
		serverConnection.addObserver(this);
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
		sendPlayerStatusChanged(player != null);
	}

	public UUID getUUID() {
		return uuid;
	}

	public void setUUID(UUID uuid) {
		clients.remove(this.uuid);
		this.uuid = uuid;
		clients.put(uuid, this);
	}

	public InetAddress getAddress() {
		return serverConnection == null ? address : serverConnection.getAddress();
	}

	public void setAddress(InetAddress address) {
		this.address = address;
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

	private void sendPlayerStatusChanged(boolean isConnected) {
		if (isConnected)
			send(MumbleMessageFactory.create(Idc.PLAYER_STATUS, true, getPlayer().getName(), getPlayer().isAdmin()));
		else
			send(MumbleMessageFactory.create(Idc.PLAYER_STATUS, false));
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
		default:
			return player != null && player.isAdmin();
		}
	}
}
