package fr.pederobien.mumble.server.event;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IRangeParameter;

public class ParameterMaxValueChangePostEvent extends ParameterEvent {
	private Object oldMaxValue;

	/**
	 * Creates an event thrown when the maximum value of a parameter has changed.
	 * 
	 * @param parameter   The parameter whose the maximum value is about to change.
	 * @param oldMaxValue The old parameter maximum value.
	 */
	public ParameterMaxValueChangePostEvent(IRangeParameter<?> parameter, Object oldMaxValue) {
		super(parameter);
		this.oldMaxValue = oldMaxValue;
	}

	@Override
	public IRangeParameter<?> getParameter() {
		return (IRangeParameter<?>) super.getParameter();
	}

	/**
	 * @return The old parameter maximum value.
	 */
	public Object getOldMaxValue() {
		return oldMaxValue;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		joiner.add("channel=" + getParameter().getSoundModifier().getChannel().getName());
		joiner.add("soundModifier=" + getParameter().getSoundModifier().getName());
		joiner.add("parameter=" + getParameter().getName());
		joiner.add("currentMaxValue=" + getParameter().getMax());
		joiner.add("oldMaxValue=" + getOldMaxValue());
		return String.format("%s_%s", getName(), joiner);
	}
}
