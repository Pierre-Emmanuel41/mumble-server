package fr.pederobien.mumble.server.persistence.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fr.pederobien.mumble.server.event.SoundModifierRegisterPostEvent;
import fr.pederobien.mumble.server.impl.SoundManager;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IParameterList;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class PendingChannelManager implements IEventListener {
	private Map<String, List<SetupChannel>> pendingChannels;

	public PendingChannelManager() {
		pendingChannels = new HashMap<String, List<SetupChannel>>();
		EventManager.registerListener(this);
	}

	/**
	 * Registers the given channel for a specified sound modifier with the specified parameters.
	 * 
	 * @param channel           The channel to registered.
	 * @param soundModifierName The channel sound modifier.
	 * @param parameterList     The parameters of the sound modifier.
	 */
	public void register(IChannel channel, String soundModifierName, IParameterList parameterList) {
		Optional<ISoundModifier> optModifier = SoundManager.getByName(soundModifierName);
		if (optModifier.isPresent()) {
			channel.setSoundModifier(optModifier.get());
			channel.getSoundModifier().getParameters().update(parameterList);
			return;
		}

		List<SetupChannel> channels = pendingChannels.get(soundModifierName);
		if (channels == null) {
			channels = new ArrayList<SetupChannel>();
			pendingChannels.put(soundModifierName, channels);
		}
		channels.add(new SetupChannel(channel, parameterList));
	}

	@EventHandler
	private void onSoundModifierRegister(SoundModifierRegisterPostEvent event) {
		List<SetupChannel> channels = pendingChannels.remove(event.getSoundModifier().getName());
		if (channels == null)
			return;

		for (SetupChannel setup : channels) {
			ISoundModifier soundModifier = event.getSoundModifier().clone();
			soundModifier.getParameters().update(setup.getParameterList());
			setup.getChannel().setSoundModifier(soundModifier);
		}
	}

	private class SetupChannel {
		private IChannel channel;
		private IParameterList parameterList;

		public SetupChannel(IChannel channel, IParameterList parameterList) {
			this.channel = channel;
			this.parameterList = parameterList;
		}

		public IChannel getChannel() {
			return channel;
		}

		public IParameterList getParameterList() {
			return parameterList;
		}
	}
}
