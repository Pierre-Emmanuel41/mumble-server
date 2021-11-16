package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IParameter;

public class ParameterValueChangePostEvent extends ParameterEvent {
	private Object oldValue;

	/**
	 * Creates an event thrown when the value of a parameter has changed.
	 * 
	 * @param parameter The parameter whose the value has changed.
	 * @param oldValue  The old parameter value.
	 */
	public ParameterValueChangePostEvent(IParameter<?> parameter, Object oldValue) {
		super(parameter);
		this.oldValue = oldValue;
	}

	/**
	 * @return The old parameter value.
	 */
	public Object getOldValue() {
		return oldValue;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("parameter=" + getParameter().getName());
		joiner.add("oldValue=" + getOldValue());
		joiner.add("currentValue=" + getParameter().getValue());
		return String.format("%s_%s", getName(), joiner);
	}
}
