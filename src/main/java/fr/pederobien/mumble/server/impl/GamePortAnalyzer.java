package fr.pederobien.mumble.server.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.messages.v10.SetGamePortUsedV10;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.LogEvent;

public class GamePortAnalyzer {
	private List<PlayerMumbleClient> clients;
	private PlayerMumbleClient client;

	/**
	 * Creates a game port analyzer that is responsible to ask each client in the specified list if a specific port is used on client
	 * side.
	 * 
	 * @param clients The list of clients to check.
	 */
	public GamePortAnalyzer(List<PlayerMumbleClient> clients) {
		this.clients = clients;
	}

	/**
	 * Send a request to each client in order to check if the given port is used on the client side.
	 *
	 * @param port The port to check.
	 *
	 * @return An optional that contains the client that use the port to play at the game if registered, an empty optional otherwise.
	 */
	public Optional<PlayerMumbleClient> checkPortByGame(int port) {
		CountDownLatch countDownLatch = new CountDownLatch(clients.size());
		for (PlayerMumbleClient client : clients)
			// Sending simultaneously the request to each client
			new Thread(() -> singleCheck(client, gamePort -> gamePort.checkPortByGame(port), countDownLatch)).start();
		try {
			countDownLatch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return Optional.empty();
		}
		return Optional.ofNullable(client);
	}

	/**
	 * Send a request to each client in order to check if the port associated to the game address of each client is used on the client
	 * side.
	 *
	 * @param connection The connection to use in order to send the request to the client..
	 *
	 * @return An optional that contains the client that use the port to play at the game if registered, an empty optional otherwise.
	 */
	public Optional<PlayerMumbleClient> checkPortByMumble(ITcpConnection connection) {
		CountDownLatch countDownLatch = new CountDownLatch(clients.size());
		for (PlayerMumbleClient client : clients)
			// Sending simultaneously the request to each client
			new Thread(() -> singleCheck(client, gamePort -> gamePort.checkPortByMumble(connection), countDownLatch)).start();
		try {
			countDownLatch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return Optional.empty();
		}
		return Optional.ofNullable(client);
	}

	private void singleCheck(PlayerMumbleClient client, Function<GamePort, Boolean> function, CountDownLatch countDownLatch) {
		GamePort gamePort = new GamePort(client);
		if (function.apply(gamePort))
			this.client = client;

		// Execution finished, notifying the waiting thread.
		countDownLatch.countDown();
		EventManager.callEvent(new LogEvent("CountDownLatch current count : %s", countDownLatch.getCount()));
	}

	private class GamePort {
		private PlayerMumbleClient client;
		private Lock lock;
		private Condition received;
		private boolean isUsed;

		public GamePort(PlayerMumbleClient client) {
			this.client = client;

			lock = new ReentrantLock();
			received = lock.newCondition();
		}

		/**
		 * Send synchronously a request to the client in order to check if the port of the specified address is used.
		 * 
		 * @param port The port to check.
		 * 
		 * @return True if the port is used on the client side, false otherwise.
		 */
		public boolean checkPortByGame(int port) {
			EventManager.callEvent(new LogEvent("Checking port n°%s", port));
			// Step 1: Sending the request to the client.
			client.send(client.createCheckGamePortMessage(port), args -> {
				if (args.isTimeout())
					isUsed = false;
				else {
					SetGamePortUsedV10 response = (SetGamePortUsedV10) MumbleServerMessageFactory.parse(args.getResponse().getBytes());
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
			}, 3000);

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

		public boolean checkPortByMumble(ITcpConnection connection) {
			EventManager.callEvent(new LogEvent("Checking port n°%s", client.getGameAddress().getPort()));
			// Step 1: Sending the request to the client.
			connection.send(new MumbleCallbackMessage(client.createCheckGamePortMessage(client.getGameAddress().getPort()), args -> {
				if (args.isTimeout())
					isUsed = false;
				else {
					SetGamePortUsedV10 response = (SetGamePortUsedV10) MumbleServerMessageFactory.parse(args.getResponse().getBytes());
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
