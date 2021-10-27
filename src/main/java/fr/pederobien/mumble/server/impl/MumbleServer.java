package fr.pederobien.mumble.server.impl;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fr.pederobien.mumble.server.exceptions.ServerNotOpenedException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.persistence.MumblePersistence;
import fr.pederobien.persistence.interfaces.IPersistence;

public class MumbleServer implements IMumbleServer {
	private InternalServer server;
	private String name;
	private IPersistence<IMumbleServer> persistence;

	/**
	 * Create a mumble server.
	 * 
	 * @param name The server name.
	 * @param port The port for TCP and UDP communication with clients.
	 * @param path The folder that contains the server configuration file.
	 */
	public MumbleServer(String name, int port, Path path) {
		this.name = name;
		this.server = new InternalServer(this, port);
		persistence = new MumblePersistence(path, this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void open() {
		server.open();
		try {
			persistence.load(getName());
		} catch (FileNotFoundException e) {
			// First time the plugin is on the server, need to add a default channel.
			addChannel("General", "default");
		}
	}

	@Override
	public void close() {
		checkIsOpened();
		persistence.save();
		server.close();
	}

	@Override
	public boolean isOpened() {
		return server.isOpened();
	}

	@Override
	public IPlayer addPlayer(InetSocketAddress address, String playerName, boolean isAdmin) {
		checkIsOpened();
		return server.getClients().addPlayer(address, playerName, isAdmin);
	}

	@Override
	public void removePlayer(String playerName) {
		checkIsOpened();
		server.getClients().removePlayer(playerName);
	}

	@Override
	public List<IPlayer> getPlayers() {
		checkIsOpened();
		return server.getClients().getPlayers();
	}

	@Override
	public IChannel addChannel(String name, String soundModifierName) {
		checkIsOpened();
		return server.addChannel(name, soundModifierName == null ? "default" : soundModifierName);
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
		return Collections.unmodifiableMap(server.getChannels());
	}

	@Override
	public List<IChannel> clearChannels() {
		return server.clearChannels();
	}

	/**
	 * @return The internal server associated to this mumble server.
	 */
	protected InternalServer getInternalServer() {
		return server;
	}

	private void checkIsOpened() {
		if (!isOpened())
			throw new ServerNotOpenedException();
	}
}
