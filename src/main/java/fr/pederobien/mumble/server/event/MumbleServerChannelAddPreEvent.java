package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.utils.ICancellable;

public class MumbleServerChannelAddPreEvent extends MumbleServerEvent implements ICancellable {
	private boolean isCancelled;
	private String channelName, soundModifierName;

	/**
	 * Creates an event thrown when a channel is about to be added added on a server.
	 * 
	 * @param server            The server to which a channel is about to be added.
	 * @param channelName       The channel name that is about to be added.
	 * @param soundModifierName The sound modifier name of the channel.
	 */
	public MumbleServerChannelAddPreEvent(IMumbleServer server, String channelName, String soundModifierName) {
		super(server);
		this.channelName = channelName;
		this.soundModifierName = soundModifierName;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * @return The channel name that is about to be added on the server.
	 */
	public String getChannelName() {
		return channelName;
	}

	/**
	 * @return The sound modifier name of the channel.
	 */
	public String getSoundModifierName() {
		return soundModifierName;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer().getName());
		joiner.add("channelName=" + getChannelName());
		joiner.add("soundModifierName=" + getSoundModifierName());
		return String.format("%s_%s", getName(), joiner);
	}
}
