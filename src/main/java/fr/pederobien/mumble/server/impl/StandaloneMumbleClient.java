package fr.pederobien.mumble.server.impl;

import java.util.Optional;

import fr.pederobien.communication.event.ConnectionLostEvent;
import fr.pederobien.communication.event.UnexpectedDataReceivedEvent;
import fr.pederobien.communication.interfaces.ITcpConnection;
import fr.pederobien.mumble.common.impl.MumbleErrorCode;
import fr.pederobien.mumble.common.impl.MumbleIdentifier;
import fr.pederobien.mumble.common.interfaces.IMumbleMessage;
import fr.pederobien.mumble.server.event.MumbleChannelNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleChannelSoundModifierChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterMaxValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterMinValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerAdminChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerGameAddressChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerKickPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerListPlayerRemovePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerNameChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerOnlineChangePostEvent;
import fr.pederobien.mumble.server.event.MumblePlayerPositionChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelAddPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerChannelRemovePostEvent;
import fr.pederobien.mumble.server.event.MumbleServerPlayerAddPostEvent;
import fr.pederobien.mumble.server.event.MumbleServerPlayerRemovePostEvent;
import fr.pederobien.mumble.server.interfaces.IPlayer;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.EventPriority;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.LogEvent;
import fr.pederobien.vocal.server.event.VocalPlayerDeafenChangePostEvent;
import fr.pederobien.vocal.server.event.VocalPlayerMuteByChangePostEvent;
import fr.pederobien.vocal.server.event.VocalPlayerMuteChangePostEvent;
import fr.pederobien.vocal.server.interfaces.IVocalPlayer;

public class StandaloneMumbleClient extends AbstractMumbleConnection implements IEventListener {

	/**
	 * Creates a client associated to the external game server.
	 * 
	 * @param server     The server associated to this client.
	 * @param connection The connection with the external game server in order to send/receive data.
	 */
	protected StandaloneMumbleClient(StandaloneMumbleServer server, ITcpConnection connection) {
		super(server, connection);

		if (establishCommunicationProtocolVersion())
			EventManager.registerListener(this);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelAdded(MumbleServerChannelAddPostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelAdd(getVersion(), event.getChannel()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelRemove(MumbleServerChannelRemovePostEvent event) {
		if (!event.getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelRemove(getVersion(), event.getChannel()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelNameChange(MumbleChannelNameChangePostEvent event) {
		if (!event.getChannel().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelNameChange(getVersion(), event.getChannel(), event.getOldName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerAdd(MumbleServerPlayerAddPostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onServerPlayerAdd(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onServerPlayerRemove(MumbleServerPlayerRemovePostEvent event) {
		if (!event.getList().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onServerPlayerRemove(getVersion(), event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerNameChange(MumblePlayerNameChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onPlayerNameChange(getVersion(), event.getOldName(), event.getPlayer().getName()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerOnlineChange(MumblePlayerOnlineChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onPlayerOnlineChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerGameAddressChange(MumblePlayerGameAddressChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onPlayerGameAddressChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerAdminChange(MumblePlayerAdminChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onPlayerAdminChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteChange(VocalPlayerMuteChangePostEvent event) {
		Optional<IPlayer> optPlayer = getMumblePlayer(event.getPlayer());
		if (!optPlayer.isPresent())
			return;

		send(getServer().getRequestManager().onPlayerMuteChange(getVersion(), optPlayer.get()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerMuteByChange(VocalPlayerMuteByChangePostEvent event) {
		Optional<IPlayer> optSource = getMumblePlayer(event.getSource());
		Optional<IPlayer> optTarget = getMumblePlayer(event.getSource());

		if (!optSource.isPresent() || !optTarget.isPresent())
			return;

		send(getServer().getRequestManager().onPlayerMuteByChange(getVersion(), optTarget.get(), optSource.get()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerDeafenChange(VocalPlayerDeafenChangePostEvent event) {
		Optional<IPlayer> optPlayer = getMumblePlayer(event.getPlayer());
		if (!optPlayer.isPresent())
			return;

		send(getServer().getRequestManager().onPlayerDeafenChange(getVersion(), optPlayer.get()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerKick(MumblePlayerKickPostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onPlayerKick(getVersion(), event.getPlayer(), event.getKickingPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onPlayerPositionChange(MumblePlayerPositionChangePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onPlayerPositionChange(getVersion(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerAdd(MumblePlayerListPlayerAddPostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelPlayerAdd(getVersion(), event.getList().getChannel(), event.getPlayer(), false));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelPlayerRemove(MumblePlayerListPlayerRemovePostEvent event) {
		if (!event.getPlayer().getServer().equals(getServer()))
			return;

		send(getServer().getRequestManager().onChannelPlayerRemove(getVersion(), event.getList().getChannel(), event.getPlayer()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterValueChange(MumbleParameterValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(getServer().getRequestManager().onParameterValueChange(getVersion(), event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMinValueChange(MumbleParameterMinValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(getServer().getRequestManager().onParameterMinValueChange(getVersion(), event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onParameterMaxValueChange(MumbleParameterMaxValueChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getParameter().getSoundModifier().getChannel()))
			return;

		send(getServer().getRequestManager().onParameterMaxValueChange(getVersion(), event.getParameter()));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onChannelSoundModifierChanged(MumbleChannelSoundModifierChangePostEvent event) {
		if (!getServer().getChannels().toList().contains(event.getChannel()))
			return;

		send(getServer().getRequestManager().onChannelSoundModifierChange(getVersion(), event.getChannel()));
	}

	@EventHandler
	private void onUnexpectedDataReceived(UnexpectedDataReceivedEvent event) {
		IMumbleMessage request = checkReceivedRequest(event);
		if (request == null)
			return;

		if (getVersion() != -1 && getVersion() != request.getHeader().getVersion()) {
			String format = "Receiving message with unexpected getVersion() of the communication protocol, expected=v%s, actual=v%s";
			EventManager.callEvent(new LogEvent(format, getVersion(), request.getHeader().getVersion()));
		} else {
			if (request.getHeader().getIdentifier() != MumbleIdentifier.UNKNOWN)
				send(getServer().getRequestManager().answer(new RequestReceivedHolder(request, this)));
			else
				send(MumbleServerMessageFactory.answer(request, MumbleErrorCode.PERMISSION_REFUSED));
		}
	}

	@EventHandler
	private void onConnectionLost(ConnectionLostEvent event) {
		if (!event.getConnection().equals(getTcpConnection()))
			return;

		getTcpConnection().dispose();
		getServer().getPlayers().clear();
		EventManager.unregisterListener(this);
	}

	/**
	 * Get the mumble player associated to the given vocal player.
	 * 
	 * @param vocalPlayer The vocal player used to get its associated mumble player.
	 * 
	 * @return An optional that contains the mumble player associated to the vocal player, or an empty optional.
	 */
	private Optional<IPlayer> getMumblePlayer(IVocalPlayer vocalPlayer) {
		Optional<IPlayer> optPlayer = getServer().getPlayers().get(vocalPlayer.getName());
		if (!optPlayer.isPresent())
			return Optional.empty();

		if (!(optPlayer.get() instanceof Player))
			return Optional.empty();

		return ((Player) optPlayer.get()).getVocalPlayer().equals(vocalPlayer) ? optPlayer : Optional.empty();
	}
}
