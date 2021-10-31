package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.function.Consumer;

import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class Client {
	private InternalServer internalServer;
	private TcpClient tcpClient;
	private UdpClient udpClient;
	private Player player;
	private UUID uuid;
	private InetSocketAddress gameAddress;

	protected Client(InternalServer internalServer, UUID uuid) {
		this.internalServer = internalServer;
		this.uuid = uuid;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof Client))
			return false;

		Client other = (Client) obj;
		return uuid.equals(other.getUUID());
	}

	/**
	 * Send a request associated to the Idc {@link Idc#PLAYER_ADMIN} to the mumble client, if defined.
	 * 
	 * @param isAdmin The new player admin status.
	 */
	public void sendAdminChanged(boolean isAdmin) {
		tryOnTcpClient(client -> client.sendAdminChanged(isAdmin));
	}

	/**
	 * Send a request associated to the Idc {@link Idc#PLAYER_INFO} to the mumble client, if defined.
	 * 
	 * @param isOnline The new player online status.
	 */
	public void sendOnlineChanged(boolean isOnline) {
		tryOnTcpClient(client -> client.sendOnlineChanged(isOnline));
	}

	/**
	 * Creates a TCP client associated to the given socket
	 * 
	 * @param socket The underlying socket used by the TCP connection to send data to the remote TCP lient.
	 */
	public void createTcpClient(ITcpConnection connection) {
		tcpClient = new TcpClient(internalServer, this, connection);
	}

	/**
	 * Creates a UDP client associated to the given connection.
	 * 
	 * @param udpServerConnection the connection used to send audio stream.
	 * @param address             The address associated to the remote UDP client.
	 * 
	 * @return The created or updated UDP client.
	 */
	public UdpClient createUdpClient(IUdpServerConnection udpServerConnection, InetSocketAddress address) {
		if (udpClient == null)
			udpClient = new UdpClient(internalServer, udpServerConnection, address);
		udpClient.setAddress(address);
		return udpClient;
	}

	/**
	 * Send a request associated to the Idc {@link Idc#PLAYER_SPEAK} to the mumble client, if defined.
	 * 
	 * @param player The speaking player.
	 * @param data   The bytes array that contains audio sample.
	 * @param global The global volume of the sample.
	 * @param left   The volume of the left channel.
	 * @param right  The volume of the right channel.
	 */
	public void onOtherPlayerSpeak(IPlayer player, byte[] data, double global, double left, double right) {
		if (udpClient != null)
			udpClient.onPlayerSpeak(player, data, global, left, right);
	}

	/**
	 * Check if the game address or the mumble address correspond to the given address and port number. If the mumble client is
	 * registered then a request is sent to know if the the given port is used.
	 * 
	 * @param port The port number used to play at the game.
	 * 
	 * @return True if the game address or the mumble address correspond to the given address and port.
	 */
	public boolean isAssociatedTo(int port) {
		boolean isAssociated = false;
		if (gameAddress != null && gameAddress.getPort() == port)
			isAssociated = true;

		if (getMumbleAddress() != null && getMumbleAddress().getPort() == port)
			isAssociated = true;

		return isAssociated;
	}

	/**
	 * @return The TCP client associated to this mumble client.
	 */
	public TcpClient getTcpClient() {
		return tcpClient;
	}

	/**
	 * @return The player associated to this client.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Set the player associated to this client.
	 * 
	 * @param player The new player.
	 */
	public void setPlayer(Player player) {
		this.player = player;
		if (player != null)
			player.setClient(this);
		else
			gameAddress = null;
	}

	/**
	 * @return The identifier of this client.
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * @return the address of the player used to play at the game. Null if the player is not connected in game.
	 */
	public InetSocketAddress getGameAddress() {
		return gameAddress;
	}

	/**
	 * Set the address of the player used to play at the game.
	 * 
	 * @param gameAddress The address used by the player to player at the game.
	 */
	public void setGameAddress(InetSocketAddress gameAddress) {
		this.gameAddress = gameAddress;
	}

	/**
	 * @return The address used by the player to speak to the other players. Null if there the player is not connected with mumble.
	 */
	public InetSocketAddress getMumbleAddress() {
		return tcpClient == null ? null : tcpClient.getAddress();
	}

	private void tryOnTcpClient(Consumer<TcpClient> consumer) {
		try {
			if (tcpClient != null)
				consumer.accept(tcpClient);
		} catch (IllegalStateException e) {
			// Do nothing
		}
	}
}
