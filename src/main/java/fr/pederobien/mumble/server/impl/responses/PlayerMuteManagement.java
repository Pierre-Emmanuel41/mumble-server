package fr.pederobien.mumble.server.impl.responses;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;

public class PlayerMuteManagement extends AbstractManagement {

	public PlayerMuteManagement(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		String playerName = event.getClient().getPlayer().getName();
		boolean isMute = (boolean) event.getRequest().getPayload()[0];
		getInternalServer().getClients().values().forEach(client -> client.onPlayerMuteStatusChanged(playerName, isMute));
		return event.getRequest().answer(event.getRequest().getPayload());
	}
}
