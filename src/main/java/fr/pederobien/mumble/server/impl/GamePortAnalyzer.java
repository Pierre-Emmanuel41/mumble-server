package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.messages.v10.GamePortSetMessageV10;

public class GamePortAnalyzer {
	private List<MumblePlayerClient> clients;
	private MumblePlayerClient client;

	/**
	 * Creates a game port analyzer that is responsible to ask each client in the specified list if a specific port is used on client
	 * side.
	 * 
	 * @param clients The list of clients to check.
	 */
	public GamePortAnalyzer(List<MumblePlayerClient> clients) {
		this.clients = clients;
	}

	/**
	 * Send a request to each client in order to check if the port associated to the given address is used on the client side.
	 * 
	 * @param address The address that contains the port to check.
	 * 
	 * @return An optional that contains the client that use the port to play at the game if registered, an empty optional otherwise.
	 */
	public Optional<MumblePlayerClient> check(InetSocketAddress address) {
		CountDownLatch countDownLatch = new CountDownLatch(clients.size());
		for (MumblePlayerClient client : clients) {
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

	private void singleCheck(MumblePlayerClient client, InetSocketAddress address, CountDownLatch countDownLatch) {
		if (new GamePort(client).check(address))
			this.client = client;

		// Execution finished, notifying the waiting thread.
		countDownLatch.countDown();
	}

	private class GamePort {
		private MumblePlayerClient client;
		private Lock lock;
		private Condition received;
		private boolean isUsed;

		public GamePort(MumblePlayerClient client) {
			this.client = client;

			lock = new ReentrantLock();
			received = lock.newCondition();
		}

		/**
		 * Send synchronously a request to the client in order to check if the port of the specified address is used.
		 * 
		 * @param address The address that contains the port to check.
		 * 
		 * @return True if the port is used on the client side, false otherwise.
		 */
		public boolean check(InetSocketAddress address) {
			address = address == null ? client.getPlayer() == null ? null : client.getPlayer().getGameAddress() : null;
			if (client.getTcpConnection() == null || address == null)
				return false;

			// Step 1: Sending the request to the client.
			client.getTcpConnection().send(new MumbleCallbackMessage(MumbleServerMessageFactory.create(Idc.GAME_PORT, address.getPort()), args -> {
				if (args.isTimeout())
					isUsed = false;
				else {
					GamePortSetMessageV10 response = (GamePortSetMessageV10) MumbleServerMessageFactory.parse(args.getResponse().getBytes());
					isUsed = response.isUsed();
				}

				// Step 3: Signaling an answer has been received.
				lock.lock();
				try {
					received.signal();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}, 3000));

			// Step 2: Waiting for an answer.
			lock.lock();
			try {
				received.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				lock.unlock();
			}
			return isUsed;
		}
	}
}
