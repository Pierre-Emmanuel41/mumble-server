package fr.pederobien.mumble.server.impl.responses;

import fr.pederobien.messenger.interfaces.IMessage;
import fr.pederobien.mumble.common.impl.Header;
import fr.pederobien.mumble.server.event.RequestEvent;
import fr.pederobien.mumble.server.impl.InternalServer;

public class PlayerDeafenManagement extends AbstractManagement {

	public PlayerDeafenManagement(InternalServer internalServer) {
		super(internalServer);
	}

	@Override
	public IMessage<Header> apply(RequestEvent event) {
		String playerName = event.getClient().getPlayer().getName();
		boolean isDeafen = (boolean) event.getRequest().getPayload()[0];
		event.getClient().getPlayer().setDeafen(isDeafen);
		getInternalServer().getClients().values().forEach(client -> client.onPlayerDeafenChanged(playerName, isDeafen));
		return event.getRequest().answer(event.getRequest().getPayload());
	}
}
