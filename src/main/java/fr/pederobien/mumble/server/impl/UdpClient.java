package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.Optional;

import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleAddressMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.PlayerSpeakPostEvent;
import fr.pederobien.mumble.server.event.PlayerSpeakPreEvent;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventManager;

public class UdpClient {
	private InternalServer internalServer;
	private IUdpServerConnection connection;
	private InetSocketAddress address;

	public UdpClient(InternalServer internalServer, IUdpServerConnection connection, InetSocketAddress address) {
		this.internalServer = internalServer;
		this.connection = connection;
		this.address = address;
	}

	/**
	 * Set the address for this udp client. It is highly probable that the address of the remote changes (when user disconnect/connect
	 * to a channel) This method allow to update the client udp address.
	 * 
	 * @param address The new client address.
	 */
	public void setAddress(InetSocketAddress address) {
		if (this.address == address)
			return;
		this.address = address;
	}

	/**
	 * Send to the mumble client a request in order to play the given bytes array with the given sound volumes.
	 * 
	 * @param player The speaking player.
	 * @param data   The bytes array that contains audio sample.
	 * @param global The global volume of the sample.
	 * @param left   The volume of the left channel.
	 * @param right  The volume of the right channel.
	 */
	public void onPlayerSpeak(IPlayer player, byte[] data, double global, double left, double right) {
		if (connection == null || connection.isDisposed())
			return;

		connection.send(new MumbleAddressMessage(MumbleMessageFactory.create(Idc.PLAYER_SPEAK, Oid.SET, player.getName(), data, global, left, right), address));
	}

	/**
	 * Dispatch to each registered channel a <code>player speak event</code> in order to send to each player registered in the same
	 * channel as the speaking player the audio sample.
	 * 
	 * @param playerName The name of the speaking player.
	 * @param data       The bytes array that contains the audio sample.
	 */
	public void onPlayerSpeak(String playerName, byte[] data) {
		Optional<Client> optClient = internalServer.getClients().getClient(playerName);
		if (!optClient.isPresent())
			return;

		EventManager.callEvent(new PlayerSpeakPreEvent(optClient.get().getPlayer(), data), new PlayerSpeakPostEvent(optClient.get().getPlayer(), data));
	}
}
