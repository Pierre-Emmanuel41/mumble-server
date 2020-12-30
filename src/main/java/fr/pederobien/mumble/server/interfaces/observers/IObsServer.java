package fr.pederobien.mumble.server.interfaces.observers;

import fr.pederobien.mumble.server.interfaces.IChannel;

public interface IObsServer {

	/**
	 * Notify this observer when a channel has been registered to this server.
	 * 
	 * @param channel the registered channel
	 */
	void onChannelAdded(IChannel channel);

	/**
	 * Notify this observer when channel has been unregistered from this server.
	 * 
	 * @param channel The unregistered channel.
	 */
	void onChannelRemoved(IChannel channel);

	/**
	 * Notify this observer the server is being closed.
	 */
	void onServerClosing();
}
