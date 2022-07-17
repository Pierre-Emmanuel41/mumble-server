package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IRangeParameter;
import fr.pederobien.utils.ICancellable;

public class MumbleParameterMaxValueChangePreEvent extends MumbleParameterEvent implements ICancellable {
	private boolean isCancelled;
	private Object newMaxValue;

	/**
	 * Creates an event thrown when the maximum value of a parameter is about to change.
	 * 
	 * @param parameter   The parameter whose the maximum value is about to change.
	 * @param newMaxValue The new parameter maximum value.
	 */
	public MumbleParameterMaxValueChangePreEvent(IRangeParameter<?> parameter, Object newMaxValue) {
		super(parameter);
		this.newMaxValue = newMaxValue;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	@Override
	public IRangeParameter<?> getParameter() {
		return (IRangeParameter<?>) super.getParameter();
	}

	/**
	 * @return The new parameter maximum value.
	 */
	public Object getNewMaxValue() {
		return newMaxValue;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("channel=" + getParameter().getSoundModifier().getChannel().getName());
		joiner.add("soundModifier=" + getParameter().getSoundModifier().getName());
		joiner.add("parameter=" + getParameter().getName());
		joiner.add("currentMaxValue=" + getParameter().getMax());
		joiner.add("newMaxValue=" + getNewMaxValue());
		return String.format("%s_%s", getName(), joiner);
	}
}
