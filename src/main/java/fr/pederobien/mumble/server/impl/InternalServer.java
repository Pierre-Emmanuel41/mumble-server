package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.event.NewTcpClientEvent;
import fr.pederobien.communication.impl.TcpServer;
import fr.pederobien.communication.impl.UdpServer;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MessageExtractor;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.ServerChannelAddPreEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.ServerChannelRemovePreEvent;
import fr.pederobien.mumble.server.event.ServerClosePostEvent;
import fr.pederobien.mumble.server.event.ServerClosePreEvent;
import fr.pederobien.mumble.server.exceptions.ChannelAlreadyExistException;
import fr.pederobien.mumble.server.exceptions.ChannelNotRegisteredException;
import fr.pederobien.mumble.server.exceptions.SoundModifierDoesNotExistException;
import fr.pederobien.mumble.server.impl.modifiers.LinearCircularSoundModifier;
import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class InternalServer implements IEventListener {
	private MumbleServer mumbleServer;
	private TcpServer tcpServer;
	private UdpServer udpServer;
	private boolean isOpened;
	private ClientList clients;
	private Map<String, Channel> channels;
	private RequestManagement requestManagement;
	private Object lockChannels;
	private int port;

	public InternalServer(MumbleServer mumbleServer, int port) {
		this.mumbleServer = mumbleServer;
		this.port = port;
		tcpServer = new TcpServer(port, () -> new MessageExtractor());
		udpServer = new UdpServer(port, () -> new MessageExtractor());

		clients = new ClientList(this);
		channels = new LinkedHashMap<String, Channel>();
		requestManagement = new RequestManagement(this);

		lockChannels = new Object();

		registerModifiers();
		EventManager.registerListener(this);
	}

	/**
	 * Starts the tcp thread and the udp thread.
	 */
	public void open() {
		tcpServer.connect();
		udpServer.connect();
		isOpened = true;
	}

	/**
	 * Interrupts the tcp thread and the udp thread.
	 */
	public void close() {
		Runnable close = () -> {
			tcpServer.disconnect();
			udpServer.disconnect();
			isOpened = false;
		};
		EventManager.callEvent(new ServerClosePreEvent(mumbleServer), close, new ServerClosePostEvent(mumbleServer));
		EventManager.unregisterListener(this);
	}

	/**
	 * @return True if the server is opened, false otherwise. The server is opened if and only if {@link #open()} method has been
	 *         called.
	 */
	public boolean isOpened() {
		return isOpened;
	}

	/**
	 * @return The port used for TCP and UDP communication.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Creates a channel associated to the given name.
	 * 
	 * @param name              The name of the channel to add.
	 * @param soundModifierName The sound modifier name attached to the channel to add.
	 * 
	 * @return The created channel.
	 * 
	 * @throws ChannelAlreadyExistException       If there is already a channel registered for the given name.
	 * @throws SoundModifierDoesNotExistException If the sound modifier name refers to no registered modifier.
	 */
	public IChannel addChannel(String name, String soundModifierName) {
		synchronized (lockChannels) {
			IChannel existingChannel = channels.get(name);
			if (existingChannel != null)
				throw new ChannelAlreadyExistException(name);

			Optional<ISoundModifier> optSoundModifier = SoundManager.getByName(soundModifierName);
			if (!optSoundModifier.isPresent())
				throw new SoundModifierDoesNotExistException(soundModifierName);

			ServerChannelAddPreEvent preEvent = new ServerChannelAddPreEvent(mumbleServer, name, soundModifierName);
			Supplier<IChannel> add = () -> {
				Channel channel = new Channel(mumbleServer, name, optSoundModifier.get());
				channels.put(channel.getName(), channel);
				return channel;
			};
			return EventManager.callEvent(preEvent, add, channel -> new ServerChannelAddPostEvent(mumbleServer, channel));
		}
	}

	/**
	 * Remove the channel associated to the given name.
	 * 
	 * @param name The name of the channel to remove.
	 * 
	 * @return The removed channel if registered, null otherwise.
	 */
	public IChannel removeChannel(String name) {
		synchronized (lockChannels) {
			return unsynchronizedRemove(name);
		}
	}

	/**
	 * Set the name of the channel associated to the specified oldName.
	 * 
	 * @param oldName The old channel name.
	 * @param newName The new channel name.
	 * 
	 * @throws ChannelNotRegisteredException If there is no channels associated to the oldName.
	 * @throws ChannelAlreadyExistException  If there is already a channel registered for the given newName.
	 */
	public void renameChannel(String oldName, String newName) {
		Map<String, IChannel> channels = getChannels();
		Channel channel = (Channel) channels.get(oldName);
		if (channel == null)
			throw new ChannelNotRegisteredException(oldName);

		if (channels.get(newName) != null)
			throw new ChannelAlreadyExistException(newName);

		channel.setName(newName);
		synchronized (lockChannels) {
			channels.remove(oldName);
			channels.put(newName, channel);
		}
	}

	/**
	 * Creates a copy of the current channels map.
	 * 
	 * @return The created copy.
	 */
	public Map<String, IChannel> getChannels() {
		synchronized (lockChannels) {
			return new LinkedHashMap<String, IChannel>(channels);
		}
	}

	/**
	 * Removes each players from each channels registered for this server.
	 * 
	 * @return The list of removed channels.
	 */
	public List<IChannel> clearChannels() {
		List<IChannel> channelsList = new ArrayList<IChannel>();
		synchronized (lockChannels) {
			List<String> names = new ArrayList<>(channels.keySet());
			int size = channels.size();
			for (int i = 0; i < size; i++)
				channelsList.add(unsynchronizedRemove(names.get(i)));
		}
		return channelsList;
	}

	/**
	 * Try to answer to the specified event.
	 * 
	 * @param event The event that contains the client which received a request from the network.
	 * 
	 * @return A message that contains the answer.
	 */
	public IMessage<Header> answer(RequestEvent event) {
		return requestManagement.answer(event);
	}

	/**
	 * @return The mumble server associated to this internalServer.
	 */
	public IMumbleServer getMumbleServer() {
		return mumbleServer;
	}

	/**
	 * @return The list of clients registered for this server.
	 */
	public ClientList getClients() {
		return clients;
	}

	private IChannel unsynchronizedRemove(String name) {
		IChannel channel = channels.get(name);
		if (channel == null)
			return null;

		ServerChannelRemovePreEvent preEvent = new ServerChannelRemovePreEvent(mumbleServer, channel);
		Supplier<IChannel> remove = () -> {
			IChannel c = channels.remove(name);
			c.clear();
			return c;
		};
		return EventManager.callEvent(preEvent, remove, c -> new ServerChannelRemovePostEvent(mumbleServer, c));
	}

	@EventHandler
	private void onNewClientConnect(NewTcpClientEvent event) {
		if (!event.getServer().equals(tcpServer))
			return;

		getClients().createClient(event.getConnection());
	}

	@EventHandler
	private void onDataReceived(DataReceivedEvent event) {
		if (!event.getConnection().equals(udpServer.getConnection()))
			return;

		IMessage<Header> response = MumbleMessageFactory.parse(event.getBuffer());
		if (response.getHeader().getIdc() != Idc.PLAYER_SPEAK || response.getHeader().getOid() != Oid.GET)
			return;

		String playerName = (String) response.getPayload()[0];
		Optional<Client> optClient = getClients().getClient(playerName);
		if (optClient.isPresent())
			optClient.get().createUdpClient(udpServer.getConnection(), event.getAddress()).onPlayerSpeak(playerName, (byte[]) response.getPayload()[1]);
	}

	private void registerModifiers() {
		SoundManager.add(new LinearCircularSoundModifier());
	}
}
