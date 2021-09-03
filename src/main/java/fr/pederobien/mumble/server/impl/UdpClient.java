package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleAddressMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class UdpClient implements IEventListener, IObsConnection {
	private InternalServer internalServer;
	private Client client;
	private IUdpServerConnection connection;
	private InetSocketAddress address;

	public UdpClient(InternalServer internalServer, Client client, IUdpServerConnection connection, InetSocketAddress address) {
		this.internalServer = internalServer;
		this.client = client;
		this.connection = connection;
		this.address = address;

		connection.addObserver(this);
		EventManager.registerListener(this);
	}

	@Override
	public void onConnectionComplete() {

	}

	@Override
	public void onConnectionDisposed() {

	}

	@Override
	public void onDataReceived(DataReceivedEvent event) {
		if (client.getPlayer() == null || client.getPlayer().getChannel() == null || !address.getAddress().equals(event.getAddress().getAddress()))
			return;

		IMessage<Header> message = MumbleMessageFactory.parse(event.getBuffer());
		if (message.getHeader().getOid() != Oid.GET)
			return;

		((Channel) client.getPlayer().getChannel()).onPlayerSpeak(client.getPlayer(), (byte[]) message.getPayload()[0]);
	}

	@Override
	public void onLog(LogEvent event) {

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
	 * Send the data associated to the given event to the player.
	 * 
	 * @param playerName The player name whose data should be sent.
	 * @param data       the byte array containing what the player said.
	 */
	public void send(String playerName, byte[] data, double global, double left, double right) {
		if (connection == null || connection.isDisposed())
			return;

		connection.send(new MumbleAddressMessage(MumbleMessageFactory.create(Idc.PLAYER_SPEAK, Oid.SET, playerName, data, global, left, right), address));
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onServerClosing(ServerClosePostEvent event) {
		if (!event.getServer().equals(internalServer.getMumbleServer()))
			return;

		EventManager.unregisterListener(this);
	}
}
