package fr.pederobien.mumble.server.external;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class MumbleGameServerThread extends Thread {
	private Semaphore semaphore;
	private MumbleGameServer externalServer;
	private ServerSocket server;
	private int port;

	protected MumbleGameServerThread(MumbleGameServer externalServer, int port) {
		this.externalServer = externalServer;
		this.port = port;
		setName("GameServerThread-");

		semaphore = new Semaphore(1);
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void start() {
		try {
			server = new ServerSocket(port, 20);
			super.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			Socket socket = server.accept();
			externalServer.createGameServerClient(socket);
			semaphore.acquire();
		} catch (IOException | InterruptedException e) {
			// When server is closing
		}
	}

	@Override
	public void interrupt() {
		try {
			server.close();
			semaphore.release();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.interrupt();
	}
}
