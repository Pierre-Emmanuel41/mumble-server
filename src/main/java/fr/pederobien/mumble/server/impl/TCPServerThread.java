package fr.pederobien.mumble.server.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import fr.pederobien.communication.impl.ServerConnection;
import fr.pederobien.mumble.common.impl.MessageExtractor;

public class TCPServerThread extends Thread {
	private InternalServer internalServer;
	private ServerSocket server;
	private InetAddress address;
	private int port;

	protected TCPServerThread(InternalServer internalServer, InetAddress address, int port) {
		this.internalServer = internalServer;
		this.address = address;
		this.port = port;
		setName("TCPThread-");
	}

	@Override
	public synchronized void start() {
		try {
			server = new ServerSocket(port, 20, address);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.start();
	}

	@Override
	public void run() {
		while (!server.isClosed()) {
			try {
				Socket socket = server.accept();
				internalServer.getOrCreateClient(socket.getInetAddress()).setConnection(new ServerConnection(socket, new MessageExtractor()));
			} catch (IOException e) {
				// When server is closing
			}
		}
	}

	@Override
	public void interrupt() {
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.interrupt();
	}
}
