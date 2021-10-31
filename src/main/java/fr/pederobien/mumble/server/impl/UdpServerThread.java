package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.impl.UdpServerConnection;
import fr.pederobien.communication.interfaces.IUdpServerConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class UdpServerThread extends Thread implements IEventListener {
	private Semaphore semaphore;
	private InternalServer internalServer;
	private IUdpServerConnection server;
	private int port;

	public UdpServerThread(InternalServer internalServer, int port) {
		this.internalServer = internalServer;
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
			server = new UdpServerConnection(new InetSocketAddress(port), 20000, () -> new MessageExtractor());
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

	@EventHandler
	public void onDataReceived(DataReceivedEvent event) {
		if (!event.getConnection().equals(server))
			return;

		IMessage<Header> response = MumbleMessageFactory.parse(event.getBuffer());
		if (response.getHeader().getIdc() != Idc.PLAYER_SPEAK || response.getHeader().getOid() != Oid.GET)
			return;

		String playerName = (String) response.getPayload()[0];
		Optional<Client> optClient = internalServer.getClients().getClient(playerName);
		if (optClient.isPresent())
			optClient.get().createUdpClient(server, event.getAddress()).onPlayerSpeak(playerName, (byte[]) response.getPayload()[1]);
	}
}
