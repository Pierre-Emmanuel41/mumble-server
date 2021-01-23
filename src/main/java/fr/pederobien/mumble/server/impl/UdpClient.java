package fr.pederobien.mumble.server.impl;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.observers.IObsServer;

public class UdpClient implements IObsServer, IObsConnection {
	private InternalServer internalServer;
	private Client client;
	private IUdpServerConnection connection;

	public UdpClient(InternalServer internalServer, Client client, IUdpServerConnection connection) {
		this.internalServer = internalServer;
		this.client = client;
		this.connection = connection;

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
		connection.dispose();
		internalServer.removeObserver(this);
	}

	@Override
	public void onConnectionComplete() {

	}

	@Override
	public void onConnectionDisposed() {

	}

	@Override
	public void onDataReceived(DataReceivedEvent event) {
		if (!event.getAddress().getAddress().equals(client.getAddress().getAddress()))
			return;
	}

	@Override
	public void onLog(LogEvent event) {

	}

	public void onOtherPlayersSpeak() {

	}
}
