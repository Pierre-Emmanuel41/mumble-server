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
import java.util.function.Supplier;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.event.ChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.ChannelNameChangePreEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPreEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePreEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyRegisteredException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;
import fr.pederobien.mumble.server.exceptions.SoundModifierDoesNotExistException;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IChannelList;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;

public class ChannelList implements IChannelList {
	private IMumbleServer server;
	private Map<String, IChannel> channels;
	private Lock lock;

	public ChannelList(IMumbleServer server) {
		this.server = server;
		channels = new LinkedHashMap<String, IChannel>();
		lock = new ReentrantLock(true);
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
		Optional<IChannel> optChannel = getChannel(channelName);
		if (optChannel.isPresent())
			throw new ChannelAlreadyRegisteredException(server, optChannel.get());

		Optional<ISoundModifier> optSoundModifier = SoundManager.getByName(soundModifierName);
		if (!optSoundModifier.isPresent())
			throw new SoundModifierDoesNotExistException(soundModifierName);

		ServerChannelAddPreEvent preEvent = new ServerChannelAddPreEvent(server, channelName, soundModifierName);
		Supplier<IChannel> add = () -> {
			Channel channel = new Channel(server, channelName, optSoundModifier.get());
			addChannel(channel);
			return channel;
		};

		return EventManager.callEvent(preEvent, add, channel -> new ServerChannelAddPostEvent(server, channel));
	}

	@Override
	public IChannel remove(String name) {
		Optional<IChannel> optChannel = getChannel(name);
		if (!optChannel.isPresent())
			return null;

		ServerChannelRemovePreEvent preEvent = new ServerChannelRemovePreEvent(server, optChannel.get());
		EventManager.callEvent(preEvent, () -> removeChannel(name), new ServerChannelRemovePostEvent(server, optChannel.get()));
		return optChannel.get();
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
				EventManager.callEvent(new ServerChannelRemovePostEvent(server, channel));
			}
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<IChannel> getChannel(String name) {
		return Optional.ofNullable(channels.get(name));
	}

	@Override
	public void renameChannel(String oldName, String newName) {
		Optional<IChannel> optChannel = getChannel(oldName);
		if (!optChannel.isPresent())
			throw new ChannelNotRegisteredException(oldName);

		Optional<IChannel> optRegistered = getChannel(newName);
		if (optRegistered.isPresent())
			throw new ChannelAlreadyRegisteredException(server, optChannel.get());

		optChannel.get().setName(newName);
	}

	@Override
	public Stream<IChannel> stream() {
		return channels.values().stream();
	}

	@Override
	public List<IChannel> toList() {
		return new ArrayList<IChannel>(channels.values());
	}

	@EventHandler(priority = EventPriority.LOW)
	private void onChannelNameChangePre(ChannelNameChangePreEvent event) {
		if (!getChannel(event.getChannel().getName()).isPresent() || !getChannel(event.getNewName()).isPresent())
			return;

		event.setCancelled(getChannel(event.getNewName()).isPresent());
	}

	@EventHandler
	private void onChannelNameChangePost(ChannelNameChangePostEvent event) {
		Optional<IChannel> optOldChannel = getChannel(event.getOldName());
		if (!optOldChannel.isPresent())
			return;

		Optional<IChannel> optNewChannel = getChannel(event.getChannel().getName());
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

	/**
	 * Thread safe operation that adds a channel to the channels list.
	 * 
	 * @param channel The channel to add.
	 * 
	 * @throws ChannelAlreadyRegisteredException if a channel is already registered for the channel name.
	 */
	private void addChannel(IChannel channel) {
		lock.lock();
		try {
			if (channels.get(channel.getName()) != null)
				throw new ChannelAlreadyRegisteredException(server, channel);

			channels.put(channel.getName(), channel);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Thread safe operation that removes a channels from the channels list.
	 * 
	 * @param channel The channel to remove.
	 * 
	 * @return The channel associated to the given name if registered, null otherwise.
	 */
	private IChannel removeChannel(String name) {
		lock.lock();
		try {
			return channels.remove(name);
		} finally {
			lock.unlock();
		}
	}
}
