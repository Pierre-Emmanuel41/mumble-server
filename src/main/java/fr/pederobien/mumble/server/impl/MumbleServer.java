package fr.pederobien.mumble.server.impl;

import fr.pederobien.mumble.server.exceptions.ServerNotOpenedException;
import fr.pederobien.mumble.server.interfaces.IChannelList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.IPlayerList;

public class MumbleServer implements IMumbleServer {
	private InternalServer server;

	/**
	 * Create a mumble server. The default communication port is 28000. In order to change the port, please turn off the server and
	 * change manually the port value in the created configuration file.
	 * 
	 * @param name The server name.
	 * @param path The folder that contains the server configuration file.
	 */
	public MumbleServer(String name, String path) {
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
	public IPlayerList getPlayers() {
		checkIsOpened();
		return server.getPlayers();
	}

	@Override
	public IChannelList getChannels() {
		checkIsOpened();
		return server.getChannels();
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
