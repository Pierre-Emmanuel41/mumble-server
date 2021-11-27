package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;

public class MumbleServer implements IMumbleServer {
	private InternalServer server;

	/**
	 * Create a mumble server.
	 * 
	 * @param name The server name.
	 * @param port The port for TCP and UDP communication with clients.
	 * @param path The folder that contains the server configuration file.
	 */
	public MumbleServer(String name, int port, Path path) {
		server = new InternalServer(name, port, path);
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
		server.close();
	}

	@Override
	public boolean isOpened() {
		return server.isOpened();
	}

	@Override
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		return server.addPlayer(address, playerName, isAdmin);
	}

	@Override
	public void removePlayer(String playerName) {
		server.removePlayer(playerName);
	}

	@Override
	public List<IPlayer> getPlayers() {
		return server.getPlayers();
	}

	@Override
	public IChannel addChannel(String name, String soundModifierName) {
		return server.addChannel(name, soundModifierName == null ? SoundManager.DEFAULT_SOUND_MODIFIER_NAME : soundModifierName);
	}

	@Override
	public IChannel removeChannel(String name) {
		return server.removeChannel(name);
	}

	@Override
	public void renameChannel(String oldName, String newName) {
		server.renameChannel(oldName, newName);
	}

	@Override
	public Map<String, IChannel> getChannels() {
		return server.getChannels();
	}

	@Override
	public List<IChannel> clearChannels() {
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

	/**
	 * @return The implementation of this server.
	 */
	protected InternalServer getInternalServer() {
		return server;
	}
}
