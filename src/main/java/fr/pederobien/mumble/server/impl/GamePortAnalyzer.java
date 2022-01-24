package fr.pederobien.mumble.server.impl;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.common.impl.Idc;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.MumbleMessageFactory;

public class GamePortAnalyzer {
	private List<MumblePlayerClient> mumblePlayerClients;
	private ITcpConnection connection;
	private MumblePlayerClient mumblePlayerClient;

	/**
	 * Creates a game port analyzer that is responsible to ask each client in the specified list if a specific port is used on client
	 * side.
	 * 
	 * @param mumblePlayerClients    The list of clients to check.
	 * @param connection The connection used to send the request to the client.
	 */
	public GamePortAnalyzer(List<MumblePlayerClient> mumblePlayerClients, ITcpConnection connection) {
		this.mumblePlayerClients = mumblePlayerClients;
		this.connection = connection;
	}

	/**
	 * Send a request to each client in order to check if the port associated to the given address is used on the client side.
	 * 
	 * @param address The address that contains the port to check.
	 * 
	 * @return An optional that contains the client that use the port to play at the game if registered, an empty optional otherwise.
	 */
	public Optional<MumblePlayerClient> check(InetSocketAddress address) {
		CountDownLatch countDownLatch = new CountDownLatch(mumblePlayerClients.size());
		for (MumblePlayerClient mumblePlayerClient : mumblePlayerClients) {
			// Sending simultaneously the request to each client
			new Thread(() -> singleCheck(mumblePlayerClient, address, countDownLatch)).start();
		}
		try {
			countDownLatch.await(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return Optional.empty();
		}
		return Optional.ofNullable(mumblePlayerClient);
	}

	private void singleCheck(MumblePlayerClient mumblePlayerClient, InetSocketAddress address, CountDownLatch countDownLatch) {
		// connection null means the client is created by the game.
		if (connection == null) {
			if (new GamePort(mumblePlayerClient.getTcpClient().getConnection()).check(address))
				this.mumblePlayerClient = mumblePlayerClient;
		}
		// connection not null means the client is created by mumble.
		else if (new GamePort(connection).check(mumblePlayerClient.getGameAddress()))
			this.mumblePlayerClient = mumblePlayerClient;

		// Execution finished, notifying the waiting thread.
		countDownLatch.countDown();
	}

	private class GamePort {
		private ITcpConnection connection;
		private Lock lock;
		private Condition received;
		private boolean isUsed;

		public GamePort(ITcpConnection connection) {
			this.connection = connection;

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
			if (address == null)
				return false;

			// Step 1: Sending the request to the client.
			connection.send(new MumbleCallbackMessage(MumbleMessageFactory.create(Idc.GAME_PORT, address.getPort()), args -> {
				if (args.isTimeout())
					isUsed = false;
				else {
					IMessage<Header> response = MumbleMessageFactory.parse(args.getResponse().getBytes());
					isUsed = (boolean) response.getPayload()[1];
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
