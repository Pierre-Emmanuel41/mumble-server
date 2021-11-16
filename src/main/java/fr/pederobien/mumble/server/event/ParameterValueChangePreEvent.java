package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.utils.ICancellable;

public class ParameterValueChangePreEvent extends ParameterEvent implements ICancellable {
	private boolean isCancelled;
	private Object currentValue, newValue;

	/**
	 * Creates an event thrown when the value of a parameter is about to change.
	 * 
	 * @param parameter    The parameter whose the value is about to change.
	 * @param currentValue The current parameter value.
	 * @param newValue     The future new parameter value.
	 */
	public ParameterValueChangePreEvent(IParameter<?> parameter, Object currentValue, Object newValue) {
		super(parameter);
		this.currentValue = currentValue;
		this.newValue = newValue;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * @return The current parameter value.
	 */
	public Object getCurrentValue() {
		return currentValue;
	}

	/**
	 * @return The future new parameter value.
	 */
	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("parameter=" + getParameter().getName());
		joiner.add("currentValue=" + getCurrentValue());
		joiner.add("newValue=" + getNewValue());
		return String.format("%s_%s", getName(), joiner);
	}
}
