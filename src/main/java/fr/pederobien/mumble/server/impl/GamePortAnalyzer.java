package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.pederobien.communication.event.DataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;
import fr.pederobien.mumble.common.impl.Oid;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;

public class GamePortAnalyzer {
	private List<Client> clients;
	private ITcpConnection connection;
	private Client client;

	/**
	 * Creates a game port analyzer that is responsible to ask each client in the specified list if a specific port is used on client
	 * side.
	 * 
	 * @param clients    The list of clients to check.
	 * @param connection The connection used to send the request to the client.
	 */
	public GamePortAnalyzer(List<Client> clients, ITcpConnection connection) {
		this.clients = clients;
		this.connection = connection;
	}

	/**
	 * Send a request to each client in order to check if the port associated to the given address is used on the client side.
	 * 
	 * @param address The address that contains the port to check.
	 * 
	 * @return An optional that contains the client that use the port to play at the game if registered, an empty optional otherwise.
	 */
	public Optional<Client> check(InetSocketAddress address) {
		CountDownLatch countDownLatch = new CountDownLatch(clients.size());
		for (Client client : clients) {
			// Sending simultaneously the request to each client
			new Thread(() -> singleCheck(client, address, countDownLatch)).start();
		}
		try {
			countDownLatch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return Optional.empty();
		}
		return Optional.ofNullable(client);
	}

	private void singleCheck(Client client, InetSocketAddress address, CountDownLatch countDownLatch) {
		// connection null means the client is created by the game.
		if (connection == null) {
			if (new GamePort(client.getTcpClient().getConnection()).check(address))
				this.client = client;
		}
		// connection not null means the client is created by mumble.
		else if (new GamePort(connection).check(client.getGameAddress()))
			this.client = client;

		// Execution finished, notifying the waiting thread.
		countDownLatch.countDown();
	}

	private class GamePort implements IEventListener {
		private ITcpConnection connection;
		private Lock lock;
		private Condition received;
		private boolean isUsed;

		public GamePort(ITcpConnection connection) {
			this.connection = connection;

			lock = new ReentrantLock();
			received = lock.newCondition();
			EventManager.registerListener(this);
		}

		/**
		 * Send synchronously a request to the client in order to check if the port of the specified address is used.
		 * 
		 * @param address The address that contains the port to check.
		 * 
		 * @return True if the port is used on the client side, false otherwise.
		 */
		public boolean check(InetSocketAddress address) {
			if (address == null)
				return false;

			lock.lock();
			try {
				connection.send(new MumbleCallbackMessage(MumbleMessageFactory.create(Idc.GAME_PORT, address.getPort()), args -> {
				}));
				if (!received.await(3000, TimeUnit.MILLISECONDS))
					isUsed = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
			return isUsed;
		}

		@EventHandler
		private void onDataReceived(DataReceivedEvent event) {
			if (!event.getConnection().equals(connection))
				return;

			IMessage<Header> response = MumbleMessageFactory.parse(event.getBuffer());
			if (response.getHeader().getIdc() != Idc.GAME_PORT && response.getHeader().getOid() != Oid.SET)
				return;

			isUsed = (boolean) response.getPayload()[1];
			EventManager.unregisterListener(this);
			lock.lock();
			try {
				received.signal();
			} finally {
				lock.unlock();
			}
		}
	}
}
