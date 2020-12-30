package fr.pederobien.mumble.server.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import fr.pederobien.mumble.server.interfaces.IChannel;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.mumble.server.interfaces.observers.IObsChannel;
import fr.pederobien.utils.Observable;

public class Channel implements IChannel {
	private String name;
	private List<IPlayer> players;
	private Observable<IObsChannel> observers;

	public Channel(String name) {
		this.name = name;
		players = new ArrayList<IPlayer>();
		observers = new Observable<IObsChannel>();
	}

	@Override
	public void addObserver(IObsChannel obs) {
		observers.addObserver(obs);
	}

	@Override
	public void removeObserver(IObsChannel obs) {
		observers.removeObserver(obs);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (this.name == name)
			return;

		String oldName = new String(this.name);
		this.name = name;
		notifyObservers(obs -> obs.onChannelRenamed(this, oldName, this.name));
	}

	@Override
	public void addPlayer(IPlayer player) {
		players.add(player);
		notifyObservers(obs -> obs.onPlayerAdded(this, player));
	}

	@Override
	public void removePlayer(IPlayer player) {
		if (players.remove(player))
			notifyObservers(obs -> obs.onPlayerRemoved(this, player));
	}

	@Override
	public List<IPlayer> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	@Override
	public void clear() {
		int size = players.size();
		for (int i = 0; i < size; i++)
			removePlayer(players.get(0));
	}

	private void notifyObservers(Consumer<IObsChannel> consumer) {
		observers.notifyObservers(consumer);
	}
}
