package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.UUID;

import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public class PlayerMumbleClient {
	private IMumbleServer server;
	private PlayerMumbleConnection playerConnection;
	private Player player;
	private UUID uuid;

	/**
	 * Creates a client associated to a specific player.
	 * 
	 * @param server The server associated to this client.
	 * @param uuid   The client unique identifier.
	 */
	protected PlayerMumbleClient(IMumbleServer server, UUID uuid) {
		this.server = server;
		this.uuid = uuid;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (!(obj instanceof PlayerMumbleClient))
			return false;

		PlayerMumbleClient other = (PlayerMumbleClient) obj;
		return uuid.equals(other.getUUID());
	}

	/**
	 * Creates a TCP client associated to the given socket
	 * 
	 * @param connection The TCP connection to send/receive data from the remote TCP client.
	 */
	public void createTcpClient(ITcpConnection connection) {
		if (playerConnection == null)
			playerConnection = new PlayerMumbleConnection(server, this, connection);
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
		return getGameAddress() != null && getGameAddress().getPort() == port || getMumbleAddress() != null && getMumbleAddress().getPort() == port;
	}

	/**
	 * @return The TCP connection with the remote.
	 */
	public ITcpConnection getTcpConnection() {
		return playerConnection == null ? null : playerConnection.getTcpConnection();
	}

	/**
	 * @return The player associated to this client. Null if not connected in game.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Set the player associated to this client.
	 * 
	 * @param player The player of this client.
	 */
	public void setPlayer(Player player) {
		this.player = player;
		if (player != null)
			player.setUUID(getUUID());
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
		return player != null && player.isOnline() ? player.getGameAddress() : null;
	}

	/**
	 * @return The address used by the player to speak to the other players. Null if there the player is not connected with mumble.
	 */
	public InetSocketAddress getMumbleAddress() {
		return playerConnection == null ? null : playerConnection.getTcpConnection().getAddress();
	}
}
