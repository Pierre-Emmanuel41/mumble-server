package fr.pederobien.mumble.server.impl;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.pederobien.mumble.server.exceptions.ServerNotOpenedException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumbleServer implements IMumbleServer {
	private InternalServer server;
	private String name;

	public MumbleServer(String name, InetAddress address, int tcpPort, int udpPort) {
		this.name = name;
		this.server = new InternalServer(address, tcpPort, udpPort);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void open() {
		server.open();
	}

	@Override
	public void close() {
		server.close();
	}

	@Override
	public boolean isOpened() {
		return server.isOpened();
	}

	@Override
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		checkIsOpened();
		return server.addPlayer(address, playerName, isAdmin);
	}

	@Override
	public void removePlayer(String playerName) {
		checkIsOpened();
		server.removePlayer(playerName);
	}

	@Override
	public List<IPlayer> getPlayers() {
		checkIsOpened();
		return server.getPlayers();
	}

	@Override
	public IChannel addChannel(String name) {
		checkIsOpened();
		return server.addChannel(name);
	}

	@Override
	public IChannel removeChannel(String name) {
		checkIsOpened();
		return server.removeChannel(name);
	}

	@Override
	public Map<String, IChannel> getChannels() {
		return Collections.unmodifiableMap(server.getChannels());
	}

	private void checkIsOpened() {
		if (!isOpened())
			throw new ServerNotOpenedException();
	}
}
