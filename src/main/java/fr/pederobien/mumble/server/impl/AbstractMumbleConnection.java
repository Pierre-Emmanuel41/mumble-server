package fr.pederobien.mumble.server.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import fr.pederobien.communication.ResponseCallbackArgs;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.MumbleCallbackMessage;
import fr.pederobien.mumble.common.impl.messages.v10.CommunicationProtocolGetMessageV10;
import fr.pederobien.mumble.common.impl.messages.v10.CommunicationProtocolSetMessageV10;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.interfaces.IMumbleServer;

public abstract class AbstractMumbleConnection {
	private IMumbleServer server;
	private ITcpConnection connection;
	private float version;

	/**
	 * Creates a mumble connection in order to send or receive requests from the remote.
	 * 
	 * @param server     The server associated to this connection.
	 * @param connection The TCP connection with the remote.
	 */
	protected AbstractMumbleConnection(IMumbleServer server, ITcpConnection connection) {
		this.server = server;
		this.connection = connection;

		version = -1;
	}

	/**
	 * @return The server associated to this mumble connection.
	 */
	public IMumbleServer getServer() {
		return server;
	}

	/**
	 * @return The version of the communication protocol to use.
	 */
	public float getVersion() {
		return version;
	}

	/**
	 * @return The TCP connection with the remote.
	 */
	protected ITcpConnection getTcpConnection() {
		return connection;
	}

	/**
	 * Set the TCP connection with the remote.
	 * 
	 * @param connection The TCP connection in order to send or receive requests from the remote.
	 */
	protected void setTcpConnection(ITcpConnection connection) {
		this.connection = connection;
	}

	/**
	 * @return true if a common version of the communication protocol has been found, false otherwise.
	 */
	protected boolean establishCommunicationProtocolVersion() {
		Lock lock = new ReentrantLock(true);
		Condition received = lock.newCondition();

		version = -1;
		getCommunicationProtocolVersion(lock, received);

		lock.lock();
		try {
			if (!received.await(5000, TimeUnit.MILLISECONDS) || version == -1) {
				connection.dispose();
				return false;
			}
		} catch (InterruptedException e) {
			// Do nothing
		} finally {
			lock.unlock();
		}

		return true;
	}

	private void getCommunicationProtocolVersion(Lock lock, Condition received) {
		// Step 1: Asking the latest version of the communication protocol supported by the remote
		send(server.getRequestManager().getCommunicationProtocolVersion(), args -> {
			if (args.isTimeout())
				// No need to wait more
				exit(lock, received);
			else {
				CommunicationProtocolGetMessageV10 message = (CommunicationProtocolGetMessageV10) MumbleServerMessageFactory.parse(args.getResponse().getBytes());
				setCommunicationProtocolVersion(lock, received, findHighestVersion(message.getVersions()));
			}
		});
	}

	private void setCommunicationProtocolVersion(Lock lock, Condition received, float version) {
		// Step 2: Setting a specific version of the communication protocol to use for the client-server communication.
		send(server.getRequestManager().setCommunicationProtocolVersion(version), args -> {
			if (!args.isTimeout()) {
				CommunicationProtocolSetMessageV10 message = (CommunicationProtocolSetMessageV10) MumbleServerMessageFactory.parse(args.getResponse().getBytes());
				if (message.getVersion() == version)
					this.version = version;
			}

			exit(lock, received);
		});
	}

	private void exit(Lock lock, Condition received) {
		lock.lock();
		try {
			received.signal();
		} finally {
			lock.unlock();
		}
	}

	private float findHighestVersion(float[] versions) {
		float version = -1;
		for (int i = versions.length - 1; 0 < i; i--) {
			if (server.getRequestManager().isSupported(versions[i])) {
				version = versions[i];
				break;
			}
		}

		return version == -1 ? 1.0f : version;
	}

	/**
	 * Send a request to the remote without expecting an answer.
	 * 
	 * @param message The request to send to the remote.
	 */
	protected void send(IMumbleMessage message) {
		send(message, null);
	}

	/**
	 * Send a request to the remote and expect an answer.
	 * 
	 * @param message  The request to send to the remote.
	 * @param callback The callback to run when an answer is received from the server.
	 */
	protected void send(IMumbleMessage message, Consumer<ResponseCallbackArgs> callback) {
		if (connection.isDisposed())
			return;

		connection.send(new MumbleCallbackMessage(message, callback));
	}
}
