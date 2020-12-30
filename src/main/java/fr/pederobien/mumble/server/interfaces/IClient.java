package fr.pederobien.mumble.server.interfaces;

import java.net.InetAddress;

public interface IClient {

	/**
	 * @return The address of this client.
	 */
	InetAddress getIp();

	/**
	 * @return The player associated to this client.
	 */
	IPlayer getPlayer();
}
