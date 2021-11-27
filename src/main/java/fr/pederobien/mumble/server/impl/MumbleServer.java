package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import fr.pederobien.mumble.server.exceptions.ServerNotOpenedException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumbleServer implements IMumbleServer {
	private InternalServer server;

	/**
	 * Create a mumble server. The default communication port is 28000. In order to change the port, please turn off the server and
	 * change manually the port value in the created configuration file.
	 * 
	 * @param name The server name.
	 * @param path The folder that contains the server configuration file.
	 */
	public MumbleServer(String name, Path path) {
		server = new InternalServer(name, path);
	}

	@Override
	public String getName() {
		return server.getName();
	}

	@Override
	public void open() {
		server.open();
	}

	@Override
	public void close() {
		checkIsOpened();
		server.close();
	}

	@Override
	public boolean isOpened() {
		checkIsOpened();
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
	public IChannel addChannel(String name, String soundModifierName) {
		checkIsOpened();
		return server.addChannel(name, soundModifierName);
	}

	@Override
	public IChannel removeChannel(String name) {
		checkIsOpened();
		return server.removeChannel(name);
	}

	@Override
	public void renameChannel(String oldName, String newName) {
		checkIsOpened();
		server.renameChannel(oldName, newName);
	}

	@Override
	public Map<String, IChannel> getChannels() {
		checkIsOpened();
		return server.getChannels();
	}

	@Override
	public List<IChannel> clearChannels() {
		checkIsOpened();
		return server.clearChannels();
	}

	@Override
	public String toString() {
		return server.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return server.equals(obj);
	}

	private void checkIsOpened() {
		if (!server.isOpened())
			throw new ServerNotOpenedException();
	}

	/**
	 * @return The implementation of this server.
	 */
	protected InternalServer getInternalServer() {
		return server;
	}
}
