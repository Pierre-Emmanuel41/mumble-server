package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IServerPlayerList;

public class ServerPlayerList implements IServerPlayerList {
	private InternalServer server;

	/**
	 * Creates a player list associated to the given server.
	 * 
	 * @param server The server to which this list is attached.
	 */
	public ServerPlayerList(InternalServer server) {
		this.server = server;
	}

	@Override
	public Iterator<IPlayer> iterator() {
		return server.getClients().getPlayers().iterator();
	}

	@Override
	public String getName() {
		return server.getName();
	}

	@Override
	public IPlayer add(String name, InetSocketAddress gameAddress, boolean isAdmin, double x, double y, double z, double yaw, double pitch) {
		return server.getClients().addPlayer(name, gameAddress, isAdmin, x, y, z, yaw, pitch);
	}

	@Override
	public IPlayer remove(String name) {
		return server.getClients().removePlayer(name);
	}

	@Override
	public boolean remove(IPlayer player) {
		return remove(player.getName()) != null;
	}

	@Override
	public void clear() {
		List<IPlayer> players = server.getClients().getPlayers();
		for (IPlayer player : players)
			remove(player);
	}

	@Override
	public Optional<IPlayer> getPlayer(String name) {
		Optional<MumblePlayerClient> optClient = server.getClients().getClient(name);
		return optClient.isPresent() ? Optional.of(optClient.get().getPlayer()) : Optional.empty();
	}

	@Override
	public Stream<IPlayer> stream() {
		return server.getClients().getPlayers().stream();
	}

	@Override
	public List<IPlayer> toList() {
		return server.getClients().getPlayers();
	}

	@Override
	public List<IPlayer> getPlayersInChannel() {
		List<IPlayer> players = new ArrayList<IPlayer>();
		for (MumblePlayerClient client : server.getClients().toList())
			if (client.getPlayer() != null && client.getPlayer().getChannel() != null)
				players.add(client.getPlayer());
		return players;
	}
}
