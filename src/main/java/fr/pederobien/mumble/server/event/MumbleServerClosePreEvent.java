package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.utils.ICancellable;

public class MumbleServerClosePreEvent extends MumbleServerEvent implements ICancellable {
	private boolean isCancelled;

	/**
	 * Creates an event thrown when a server is about to be closed.
	 * 
	 * @param server The server that is about to be closed.
	 */
	public MumbleServerClosePreEvent(IMumbleServer server) {
		super(server);
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("server=" + getServer().getName());
		return String.format("%s_%s", getName(), joiner);
	}
}
