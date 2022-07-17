package fr.pederobien.mumble.server.impl;

import fr.pederobien.mumble.server.event.MumbleChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleChannelNameChangePreEvent;
import fr.pederobien.mumble.server.event.MumbleChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleChannelSoundModifierChangePreEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerClosePostEvent;
import fr.pederobien.mumble.server.impl.modifiers.SoundModifier;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Channel implements IChannel, IEventListener {
	private IMumbleServer server;
	private String name;
	private IChannelPlayerList players;
	private ISoundModifier soundModifier;

	/**
	 * Creates a channel based on the given parameter.
	 * 
	 * @param server        The mumble server to which this channel is attached.
	 * @param name          The channel name.
	 * @param soundModifier The channel sound modifier.
	 */
	public Channel(IMumbleServer server, String name, ISoundModifier soundModifier) {
		this.server = server;
		this.name = name;
		this.soundModifier = soundModifier;
		((SoundModifier) soundModifier).setChannel(this);

		players = new ChannelPlayerList(this);

		EventManager.registerListener(this);
	}

	@Override
	public IMumbleServer getServer() {
		return server;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (this.name.equals(name))
			return;

		String oldName = this.name;
		EventManager.callEvent(new MumbleChannelNameChangePreEvent(this, name), () -> this.name = name, new MumbleChannelNameChangePostEvent(this, oldName));
	}

	@Override
	public IChannelPlayerList getPlayers() {
		return players;
	}

	@Override
	public ISoundModifier getSoundModifier() {
		return soundModifier;
	}

	@Override
	public void setSoundModifier(ISoundModifier soundModifier) {
		if (this.soundModifier.equals(soundModifier))
			return;

		ISoundModifier futur = soundModifier == null ? SoundManager.getDefaultSoundModifier() : soundModifier;
		SoundModifier oldSoundModifier = (SoundModifier) this.soundModifier;
		Runnable set = () -> {
			oldSoundModifier.setChannel(null);
			this.soundModifier = futur;
			((SoundModifier) this.soundModifier).setChannel(this);
		};
		MumbleChannelSoundModifierChangePreEvent preEvent = new MumbleChannelSoundModifierChangePreEvent(this, getSoundModifier(), futur);
		MumbleChannelSoundModifierChangePostEvent postEvent = new MumbleChannelSoundModifierChangePostEvent(this, oldSoundModifier);
		EventManager.callEvent(preEvent, set, postEvent);
	}

	@Override
	public String toString() {
		return "Channel={" + name + "}";
	}

	@EventHandler
	private void onServerClosing(MumbleServerClosePostEvent event) {
		if (!event.getServer().equals(server))
			return;

		players.clear();
		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onChannelRemove(MumbleServerChannelRemovePostEvent event) {
		if (!event.getChannel().equals(this))
			return;

		EventManager.unregisterListener(this);
	}
}
