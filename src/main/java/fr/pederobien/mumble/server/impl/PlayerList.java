package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.IPlayerList;

public class PlayerList implements IPlayerList {
	private InternalServer server;

	public PlayerList(InternalServer server) {
		this.server = server;
	}

	@Override
	public Iterator<IPlayer> iterator() {
		return server.getPlayers().iterator();
	}

	@Override
	public String getName() {
		return server.getName();
	}

	@Override
	public IPlayer add(InetSocketAddress address, String playerName, boolean isAdmin) {
		return server.getClients().addPlayer(address, playerName, isAdmin);
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
	public Optional<IPlayer> getPlayer(String name) {
		Optional<Client> optClient = server.getClients().getClient(name);
		return !optClient.isPresent() ? Optional.empty() : Optional.of(optClient.get().getPlayer());
	}

	@Override
	public List<IPlayer> getPlayersInChannel() {
		List<IPlayer> players = new ArrayList<IPlayer>();
		for (Client client : server.getClients().getClients())
			if (client.getPlayer() != null && client.getPlayer().getChannel() != null)
				players.add(client.getPlayer());
		return players;
	}

	@Override
	public Stream<IPlayer> stream() {
		return server.getClients().getPlayers().stream();
	}

	@Override
	public List<IPlayer> toList() {
		return server.getClients().getPlayers();
	}
}
