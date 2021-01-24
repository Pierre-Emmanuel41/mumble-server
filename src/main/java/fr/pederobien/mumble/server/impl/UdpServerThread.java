package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.LogEvent;
import fr.pederobien.communication.impl.UdpServerConnection;
import fr.pederobien.communication.interfaces.IObsConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.mumble.common.impl.MessageExtractor;

public class UdpServerThread extends Thread implements IObsConnection {
	private Semaphore semaphore;
	private InternalServer internalServer;
	private IUdpServerConnection server;
	private InetAddress address;
	private int port;

	public UdpServerThread(InternalServer internalServer, InetAddress address, int port) {
		this.internalServer = internalServer;
		this.address = address;
		this.port = port;
		setName("TCPThread-");

		semaphore = new Semaphore(1);
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			server = new UdpServerConnection(new InetSocketAddress(address, port), () -> new MessageExtractor());
			server.addObserver(this);
			semaphore.acquire();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// When server is closing
		}
	}

	@Override
	public void interrupt() {
		server.dispose();
		semaphore.release();
	}

	@Override
	public void onConnectionComplete() {

	}

	@Override
	public void onConnectionDisposed() {

	}

	@Override
	public void onDataReceived(DataReceivedEvent event) {
		Client client = internalServer.getOrCreateClient(event.getAddress());
		client.createUdpClient(server, event.getAddress());
	}

	@Override
	public void onLog(LogEvent event) {

	}
}
