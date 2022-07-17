package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IRangeParameter;
import fr.pederobien.utils.ICancellable;

public class MumbleParameterMinValueChangePreEvent extends MumbleParameterEvent implements ICancellable {
	private boolean isCancelled;
	private Object newMinValue;

	/**
	 * Creates an event thrown when the minimum value of a parameter is about to change.
	 * 
	 * @param parameter   The parameter whose the minimum value is about to change.
	 * @param newMinValue The new parameter minimum value.
	 */
	public MumbleParameterMinValueChangePreEvent(IRangeParameter<?> parameter, Object newMinValue) {
		super(parameter);
		this.newMinValue = newMinValue;
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
	 * @return The new parameter minimum value.
	 */
	public Object getNewMinValue() {
		return newMinValue;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("channel=" + getParameter().getSoundModifier().getChannel().getName());
		joiner.add("soundModifier=" + getParameter().getSoundModifier().getName());
		joiner.add("parameter=" + getParameter().getName());
		joiner.add("currentMinValue=" + getParameter().getMin());
		joiner.add("newMinValue=" + getNewMinValue());
		return String.format("%s_%s", getName(), joiner);
	}
}
