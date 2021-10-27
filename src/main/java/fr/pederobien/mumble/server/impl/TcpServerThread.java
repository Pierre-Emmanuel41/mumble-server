package fr.pederobien.mumble.server.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerThread extends Thread {
	private InternalServer internalServer;
	private ServerSocket server;
	private int port;

	protected TcpServerThread(InternalServer internalServer, int port) {
		this.internalServer = internalServer;
		this.port = port;
		setName("TCPThread-");
	}

	@Override
	public synchronized void start() {
		try {
			server = new ServerSocket(port, 20);
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
				internalServer.getClients().createClient(socket);
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
