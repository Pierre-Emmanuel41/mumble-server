package fr.pederobien.mumble.server.impl;

import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelNameChangePreEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelSoundModifierChangePreEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.impl.modifiers.SoundModifier;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IChannelPlayerList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class Channel implements IChannel, IEventListener {
	private IMumbleServer mumbleServer;
	private String name;
	private IChannelPlayerList players;
	private ISoundModifier soundModifier;

	/**
	 * Creates a channel based on the given parameter.
	 * 
	 * @param mumbleServer  The mumble server to which this channel is attached.
	 * @param name          The channel name.
	 * @param soundModifier The channel sound modifier.
	 */
	public Channel(IMumbleServer mumbleServer, String name, ISoundModifier soundModifier) {
		this.mumbleServer = mumbleServer;
		this.name = name;
		this.soundModifier = soundModifier;
		((SoundModifier) soundModifier).setChannel(this);

		players = new ChannelPlayerList(this);

		EventManager.registerListener(this);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (this.name == name)
			return;

		String oldName = this.name;
		EventManager.callEvent(new ChannelNameChangePreEvent(this, name), () -> this.name = name, new ChannelNameChangePostEvent(this, oldName));
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
		ChannelSoundModifierChangePreEvent preEvent = new ChannelSoundModifierChangePreEvent(this, getSoundModifier(), futur);
		ChannelSoundModifierChangePostEvent postEvent = new ChannelSoundModifierChangePostEvent(this, oldSoundModifier);
		EventManager.callEvent(preEvent, set, postEvent);
	}

	@Override
	public String toString() {
		return "Channel={" + name + "}";
	}

	@EventHandler
	private void onServerClosing(ServerClosePostEvent event) {
		if (!event.getServer().equals(mumbleServer))
			return;

		players.clear();
		EventManager.unregisterListener(this);
	}

	@EventHandler
	private void onChannelRemove(ServerChannelRemovePostEvent event) {
		if (!event.getChannel().equals(this))
			return;

		EventManager.unregisterListener(this);
	}
}
