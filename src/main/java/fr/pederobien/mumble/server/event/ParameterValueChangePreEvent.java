package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.utils.ICancellable;

public class ParameterValueChangePreEvent extends ParameterEvent implements ICancellable {
	private boolean isCancelled;
	private Object newValue;

	/**
	 * Creates an event thrown when the value of a parameter is about to change.
	 * 
	 * @param parameter The parameter whose the value is about to change.
	 * @param newValue  The future new parameter value.
	 */
	public ParameterValueChangePreEvent(IParameter<?> parameter, Object newValue) {
		super(parameter);
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
	 * @return The future new parameter value.
	 */
	public Object getNewValue() {
		return newValue;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("channel=" + getParameter().getSoundModifier().getChannel().getName());
		joiner.add("soundModifier=" + getParameter().getSoundModifier().getName());
		joiner.add("parameter=" + getParameter().getName());
		joiner.add("currentValue=" + getParameter().getValue());
		joiner.add("newValue=" + getNewValue());
		return String.format("%s_%s", getName(), joiner);
	}
}
