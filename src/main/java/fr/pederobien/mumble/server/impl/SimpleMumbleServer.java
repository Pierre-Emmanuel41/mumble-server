package fr.pederobien.mumble.server.impl;

import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.pederobien.mumble.server.event.MumbleServerClosePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClosePreEvent;
import fr.pederobien.mumble.server.event.MumbleServerOpenPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerOpenPreEvent;
import fr.pederobien.mumble.server.persistence.SimpleMumbleServerPersistence;
import fr.pederobien.utils.event.EventManager;

public class SimpleMumbleServer extends AbstractMumbleServer {
	private SimpleMumbleServerPersistence persistence;
	private AtomicBoolean isOpened;

	/**
	 * Create a mumble server that should be integrated in the game server in order to allow server configuration modification
	 * directly from the game. The default communication port is 28000. In order to change the port, please turn off the server and
	 * change manually the port value in the created configuration file.
	 * 
	 * @param name The server name.
	 * @param path The folder that contains the server configuration file.
	 */
	public SimpleMumbleServer(String name, String path) {
		super(name);

		isOpened = new AtomicBoolean(false);

		persistence = new SimpleMumbleServerPersistence(path);
		persistence.deserialize(this);
	}

	@Override
	public void open() {
		if (!isOpened.compareAndSet(false, true))
			return;

		EventManager.callEvent(new MumbleServerOpenPreEvent(this), () -> super.open(), new MumbleServerOpenPostEvent(this));
	}

	@Override
	public void close() {
		if (!isOpened.compareAndSet(true, false))
			return;

		Runnable update = () -> {
			super.close();
			persistence.serialize(this);
		};
		EventManager.callEvent(new MumbleServerClosePreEvent(this), update, new MumbleServerClosePostEvent(this));
	}

	@Override
	public boolean isOpened() {
		return isOpened.get();
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("name=" + getName());
		joiner.add(String.format("configuration port = %s", getConfigurationPort()));
		joiner.add(String.format("vocal port = %s", getVocalPort()));
		return joiner.toString();
	}
}
