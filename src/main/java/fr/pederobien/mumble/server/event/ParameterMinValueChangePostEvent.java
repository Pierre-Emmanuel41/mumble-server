package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IRangeParameter;

public class ParameterMinValueChangePostEvent extends ParameterEvent {
	private Object oldMinValue;

	/**
	 * Creates an event thrown when the minimum value of a parameter has changed.
	 * 
	 * @param parameter   The parameter whose the minimum value is about to change.
	 * @param oldMinValue The old parameter minimum value.
	 */
	public ParameterMinValueChangePostEvent(IRangeParameter<?> parameter, Object oldMinValue) {
		super(parameter);
		this.oldMinValue = oldMinValue;
	}

	@Override
	public IRangeParameter<?> getParameter() {
		return (IRangeParameter<?>) super.getParameter();
	}

	/**
	 * @return The old parameter minimum value.
	 */
	public Object getOldMinValue() {
		return oldMinValue;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("channel=" + getParameter().getSoundModifier().getChannel().getName());
		joiner.add("soundModifier=" + getParameter().getSoundModifier().getName());
		joiner.add("parameter=" + getParameter().getName());
		joiner.add("currentMinValue=" + getParameter().getMin());
		joiner.add("oldMinValue=" + getOldMinValue());
		return String.format("%s_%s", getName(), joiner);
	}
}
