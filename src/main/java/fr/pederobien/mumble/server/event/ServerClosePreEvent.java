package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.utils.ICancellable;

public class ServerClosePreEvent extends ServerEvent implements ICancellable {
	private boolean isCancelled;

	/**
	 * Creates an event thrown when a server is about to be closed.
	 * 
	 * @param server The server that is about to be closed.
	 */
	public ServerClosePreEvent(IMumbleServer server) {
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
}
