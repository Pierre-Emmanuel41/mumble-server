package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.impl.UdpServerConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;

public class UdpServerThread extends Thread implements IEventListener {
	private Semaphore semaphore;
	private InternalServer internalServer;
	private IUdpServerConnection server;
	private InetAddress address;
	private int port;

	public UdpServerThread(InternalServer internalServer, InetAddress address, int port) {
		this.internalServer = internalServer;
		this.address = address;
		this.port = port;
		setName("UDPThread-");

		semaphore = new Semaphore(1);
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		EventManager.registerListener(this);
	}

	@Override
	public void run() {
		try {
			server = new UdpServerConnection(new InetSocketAddress(address, port), 20000, () -> new MessageExtractor());
			semaphore.acquire();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// When server is closing
		}

		EventManager.unregisterListener(this);
	}

	@Override
	public void interrupt() {
		server.dispose();
		semaphore.release();
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDataReceived(DataReceivedEvent event) {
		Client client = internalServer.getOrCreateClient(event.getAddress());
		client.createUdpClient(server, event.getAddress());
	}
}
