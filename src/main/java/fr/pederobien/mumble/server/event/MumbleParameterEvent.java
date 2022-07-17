package fr.pederobien.mumble.server.event;

import fr.pederobien.mumble.server.interfaces.IParameter;

public class MumbleParameterEvent extends ProjectMumbleServerEvent {
	private IParameter<?> parameter;

	/**
	 * Creates a parameter event.
	 * 
	 * @param parameter The parameter source involved in this event.
	 */
	public MumbleParameterEvent(IParameter<?> parameter) {
		this.parameter = parameter;
	}

	/**
	 * @return The parameter involved in this event.
	 */
	public IParameter<?> getParameter() {
		return parameter;
	}
}
