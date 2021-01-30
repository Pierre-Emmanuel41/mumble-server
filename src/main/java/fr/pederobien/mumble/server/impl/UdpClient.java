package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleAddressMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;

public class UdpClient implements IObsServer, IObsConnection {
	private InternalServer internalServer;
	private Client client;
	private IUdpServerConnection connection;
	private InetSocketAddress address;

	public UdpClient(InternalServer internalServer, Client client, IUdpServerConnection connection, InetSocketAddress address) {
		this.internalServer = internalServer;
		this.client = client;
		this.connection = connection;
		this.address = address;

		internalServer.addObserver(this);
		connection.addObserver(this);
	}

	@Override
	public void onChannelAdded(IChannel channel) {

	}

	@Override
	public void onChannelRemoved(IChannel channel) {

	}

	@Override
	public void onServerClosing() {
		internalServer.removeObserver(this);
		connection.removeObserver(this);
	}

	@Override
	public void onConnectionComplete() {

	}

	@Override
	public void onConnectionDisposed() {

	}

	@Override
	public void onDataReceived(DataReceivedEvent event) {
		if ((client.getChannel() == null || !address.getAddress().equals(event.getAddress().getAddress())))
			return;

		byte[] data = (byte[]) MumbleMessageFactory.parse(event.getBuffer()).getPayload()[0];
		client.getChannel().onPlayerSpeak(client.getPlayer(), data);
	}

	@Override
	public void onLog(LogEvent event) {

	}

	/**
	 * Send the data associated to the given event to the player.
	 * 
	 * @param event The event that contains data to send.
	 */
	public void send(byte[] data) {
		if (connection == null || connection.isDisposed())
			return;

		connection.send(new MumbleAddressMessage(MumbleMessageFactory.create(Idc.PLAYER_SPEAK, Oid.SET, data), address));
	}
}
