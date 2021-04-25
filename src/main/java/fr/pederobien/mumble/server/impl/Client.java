package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import fr.pederobien.communication.impl.TcpServerConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.mumble.common.impl.MessageExtractor;

public class Client {
	private InternalServer internalServer;
	private TcpClient tcpClient;
	private UdpClient udpClient;
	private Player player;
	private UUID uuid;
	private InetSocketAddress address;
	private Channel channel;

	protected Client(InternalServer internalServer, UUID uuid, InetSocketAddress address) {
		this.internalServer = internalServer;
		this.uuid = uuid;
		this.address = address;
	}

	public void sendAdminChanged(boolean isAdmin) {
		if (tcpClient != null)
			tcpClient.sendAdminChanged(isAdmin);
	}

	public void sendPlayerStatusChanged(boolean isConnected) {
		if (tcpClient != null)
			tcpClient.sendPlayerStatusChanged(isConnected);
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

	public void createTcpClient(Socket socket) {
		tcpClient = new TcpClient(internalServer, this, new TcpServerConnection(socket, new MessageExtractor()));
	}

	public void createUdpClient(IUdpServerConnection udpServerConnection, InetSocketAddress address) {
		if (udpClient == null)
			udpClient = new UdpClient(internalServer, this, udpServerConnection, address);
		udpClient.setAddress(address);
	}

	public void onOtherPlayerSpeak(String playerName, byte[] data, double global, double left, double right) {
		if (udpClient != null)
			udpClient.send(playerName, data, global, left, right);
	}

	public void onPlayerMuteStatusChanged(String playerName, boolean isMute) {
		tcpClient.sendPlayerMuteChanged(playerName, isMute);
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
		return tcpClient == null ? address : tcpClient.getAddress();
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
}
