package fr.pederobien.mumble.server.impl;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPServerThread extends Thread {
	private InternalServer internalServer;
	private DatagramSocket server;
	private InetAddress address;
	private int port;

	public UDPServerThread(InternalServer internalServer, InetAddress address, int port) {
		this.internalServer = internalServer;
		this.address = address;
		this.port = port;
		setName("UDPThread-");
	}
}
