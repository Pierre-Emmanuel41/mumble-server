package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.event.MumbleChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelAddPreEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelRemovePreEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyRegisteredException;
import fr.pederobien.mumble.server.exceptions.SoundModifierDoesNotExistException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IChannelList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class ChannelList implements IChannelList, IEventListener {
	private IMumbleServer server;
	private Map<String, IChannel> channels;
	private Lock lock;

	public ChannelList(IMumbleServer server) {
		this.server = server;
		channels = new LinkedHashMap<String, IChannel>();
		lock = new ReentrantLock(true);

		EventManager.registerListener(this);
	}

	@Override
	public Iterator<IChannel> iterator() {
		return channels.values().iterator();
	}

	@Override
	public IMumbleServer getServer() {
		return server;
	}

	@Override
	public IChannel add(String channelName, String soundModifierName) {
		lock.lock();
		try {
			Optional<IChannel> optChannel = get(channelName);
			if (optChannel.isPresent())
				throw new ChannelAlreadyRegisteredException(server, optChannel.get());

			Optional<ISoundModifier> optSoundModifier = SoundManager.getByName(soundModifierName);
			if (!optSoundModifier.isPresent())
				throw new SoundModifierDoesNotExistException(soundModifierName);

			IChannel channel = new Channel(server, channelName, optSoundModifier.get());
			MumbleServerChannelAddPreEvent preEvent = new MumbleServerChannelAddPreEvent(server, channelName, soundModifierName);
			Runnable update = () -> channels.put(channel.getName(), channel);
			EventManager.callEvent(preEvent, update, new MumbleServerChannelAddPostEvent(server, channel));
			return channel;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public IChannel remove(String name) {
		lock.lock();
		try {
			Optional<IChannel> optChannel = get(name);
			if (!optChannel.isPresent())
				return null;

			IChannel channel = optChannel.get();
			Runnable update = () -> {
				channels.remove(name);
				channel.getPlayers().clear();
			};
			EventManager.callEvent(new MumbleServerChannelRemovePreEvent(server, channel), update, new MumbleServerChannelRemovePostEvent(server, channel));
			return channel;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean remove(IChannel channel) {
		return remove(channel.getName()) != null;
	}

	@Override
	public void clear() {
		lock.lock();
		try {
			Set<String> names = new HashSet<String>(channels.keySet());
			for (String name : names) {
				IChannel channel = channels.remove(name);
				channel.getPlayers().clear();
				EventManager.callEvent(new MumbleServerChannelRemovePostEvent(server, channel));
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<IChannel> get(String name) {
		return Optional.ofNullable(channels.get(name));
	}

	@Override
	public Stream<IChannel> stream() {
		return channels.values().stream();
	}

	@Override
	public List<IChannel> toList() {
		return new ArrayList<IChannel>(channels.values());
	}

	@EventHandler
	private void onChannelNameChangePost(MumbleChannelNameChangePostEvent event) {
		Optional<IChannel> optOldChannel = get(event.getOldName());
		if (!optOldChannel.isPresent())
			return;

		Optional<IChannel> optNewChannel = get(event.getChannel().getName());
		if (optNewChannel.isPresent())
			throw new ChannelAlreadyRegisteredException(server, optNewChannel.get());

		lock.lock();
		try {
			channels.remove(event.getOldName());
			channels.put(event.getChannel().getName(), event.getChannel());
		} finally {
			lock.unlock();
		}
	}
}
