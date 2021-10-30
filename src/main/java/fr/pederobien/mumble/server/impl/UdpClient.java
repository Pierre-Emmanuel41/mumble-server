package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleAddressMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.PlayerSpeakPostEvent;
import fr.pederobien.mumble.server.event.PlayerSpeakPreEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class UdpClient implements IEventListener {
	private InternalServer internalServer;
	private Client client;
	private IUdpServerConnection connection;
	private InetSocketAddress address;

	public UdpClient(InternalServer internalServer, Client client, IUdpServerConnection connection, InetSocketAddress address) {
		this.internalServer = internalServer;
		this.client = client;
		this.connection = connection;
		this.address = address;

		EventManager.registerListener(this);
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

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		if (!event.getServer().equals(internalServer.getMumbleServer()))
			return;

		EventManager.unregisterListener(this);
	}

	@EventHandler
	public void onDataReceived(DataReceivedEvent event) {
		if (!event.getConnection().equals(connection))
			return;

		if (client.getPlayer() == null || client.getPlayer().getChannel() == null || !address.getAddress().equals(event.getAddress().getAddress()))
			return;

		IMessage<Header> message = MumbleMessageFactory.parse(event.getBuffer());
		if (message.getHeader().getOid() != Oid.GET)
			return;

		byte[] data = (byte[]) message.getPayload()[0];
		EventManager.callEvent(new PlayerSpeakPreEvent(client.getPlayer(), data), new PlayerSpeakPostEvent(client.getPlayer(), data));
	}
}
