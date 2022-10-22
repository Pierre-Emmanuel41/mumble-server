package fr.pederobien.mumble.server.impl.modifiers;

import java.util.StringJoiner;

import fr.pederobien.mumble.common.impl.ParameterType;
import fr.pederobien.mumble.server.event.MumbleParameterMaxValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterMaxValueChangePreEvent;
import fr.pederobien.mumble.server.event.MumbleParameterMinValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterMinValueChangePreEvent;
import fr.pederobien.mumble.server.event.MumbleParameterValueChangePostEvent;
import fr.pederobien.mumble.server.interfaces.IRangeParameter;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventManager;

public class RangeParameter<T> extends Parameter<T> implements IRangeParameter<T> {
	private T min, max;

	/**
	 * Creates a range parameter. A range is associated to this parameter and when the value is changed the range validate or
	 * invalidate the modification.
	 * 
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param value         The parameter value.
	 * @param defaultValue  The default parameter value.
	 * @param min           The minimum parameter value.
	 * @param max           The maximum parameter value.
	 */
	public static <T> RangeParameter<T> of(ISoundModifier soundModifier, String name, T defaultValue, T value, T min, T max) {
		return new RangeParameter<T>(soundModifier, name, defaultValue, value, min, max);
	}

	/**
	 * Creates a range parameter. A range is associated to this parameter and when the value is changed the range validate or
	 * invalidate the modification.
	 * 
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param defaultValue  The default parameter value.
	 * @param min           The minimum parameter value.
	 * @param max           The maximum parameter value.
	 */
	public static <T> RangeParameter<T> of(ISoundModifier soundModifier, String name, T defaultValue, T min, T max) {
		return of(soundModifier, name, defaultValue, defaultValue, min, max);
	}

	/**
	 * Creates a new parameter based on the given parameters. The parameter type is used to parse correctly the string representation
	 * of the defaultValue and value.
	 * 
	 * @param <T>          The type of this parameter.
	 * @param type         the type of this parameter.
	 * @param name         The parameter name.
	 * @param defaultValue the parameter default value.
	 * @param value        The parameter value.
	 * @param min          The minimum of the range associated to the created range parameter.
	 * @param max          The maximum of the range associated to the created range parameter.
	 * @return The created parameter initialized with the given parameters.
	 */
	public static <T> RangeParameter<T> fromType(ParameterType<T> type, String name, String defaultValue, String value, String min, String max) {
		return of(null, name, type.getValue(defaultValue), type.getValue(value), type.getValue(min), type.getValue(max));
	}

	/**
	 * Creates a new parameter based on the given parameters. The parameter type is used to parse correctly the string representation
	 * of the defaultValue and value.
	 * 
	 * @param <T>          The type of this parameter.
	 * @param type         the type of this parameter.
	 * @param name         The parameter name.
	 * @param defaultValue the parameter default value.
	 * @param value        The parameter value.
	 * @param min          The minimum of the range associated to the created range parameter.
	 * @param max          The maximum of the range associated to the created range parameter.
	 * @return The created parameter initialized with the given parameters.
	 */
	public static <T> RangeParameter<T> fromType(ParameterType<T> type, String name, Object defaultValue, Object value, Object min, Object max) {
		return of(null, name, type.cast(defaultValue), type.cast(value), type.cast(min), type.cast(max));
	}

	/**
	 * Creates a range parameter. A range is associated to this parameter and when the value is changed the range validate or
	 * invalidate the modification.
	 * 
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param value         The parameter value.
	 * @param defaultValue  The default parameter value.
	 * @param min           The minimum parameter value.
	 * @param max           The maximum parameter value.
	 */
	protected RangeParameter(ISoundModifier soundModifier, String name, T defaultValue, T value, T min, T max) {
		super(soundModifier, name, defaultValue, value);
		if (RANGE_TYPES.get(value.getClass()) == null)
			throw new IllegalArgumentException("The type of the generic parameter must not be neither boolean nor character.");

		this.min = min;
		this.max = max;

		// The minimum should always be less than the maximum value
		check(min, max, "The minimum value should be less than the maximum value.");
		checkRange(value);
	}

	/**
	 * Private constructor for clone method.
	 * 
	 * @param original The original parameter to clone.
	 */
	private RangeParameter(RangeParameter<T> original) {
		super(original);
		min = original.getMin();
		max = original.getMax();
	}

	@Override
	public void setValue(Object value) {
		checkRange(getType().cast(value));
		super.setValue(value);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("name=" + getName());
		joiner.add("value=" + getValue());
		joiner.add("defaultValue=" + getDefaultValue());
		joiner.add("type=" + getType());
		joiner.add(String.format("range=[%s, %s]", min, max));
		return joiner.toString();
	}

	@Override
	public RangeParameter<T> clone() {
		return new RangeParameter<T>(this);
	}

	@Override
	public T getMin() {
		return min;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setMin(Object min) {
		T castMin = getType().cast(min);
		if (this.min.equals(castMin))
			return;

		check(castMin, getMax(), "The minimum value cannot be greater than the maximum value");

		Comparable<? super Number> comparableMin = (Comparable<? super Number>) min;
		if (!isAttached()) {
			if (comparableMin.compareTo((Number) getValue()) > 0)
				setValue0(castMin);
			this.min = castMin;
		} else {
			T oldMin = this.min;
			Runnable update = () -> {
				if (comparableMin.compareTo((Number) getValue()) > 0) {
					T oldValue = getValue();
					setValue0(castMin);
					EventManager.callEvent(new MumbleParameterValueChangePostEvent(this, oldValue));
				}
				this.min = castMin;
			};
			EventManager.callEvent(new MumbleParameterMinValueChangePreEvent(this, min), update, new MumbleParameterMinValueChangePostEvent(this, oldMin));
		}
	}

	@Override
	public T getMax() {
		return max;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void setMax(Object max) {
		T castMax = getType().cast(max);
		if (this.max.equals(castMax))
			return;

		check(getMin(), castMax, "The minimum value cannot be greater than the maximum value");

		Comparable<? super Number> comparableMax = (Comparable<? super Number>) max;
		if (!isAttached()) {
			if (comparableMax.compareTo((Number) getValue()) < 0)
				setValue0(castMax);
			this.max = castMax;
		} else {
			T oldMax = this.max;
			Runnable update = () -> {
				if (comparableMax.compareTo((Number) getValue()) < 0) {
					T oldValue = getValue();
					setValue0(castMax);
					EventManager.callEvent(new MumbleParameterValueChangePostEvent(this, oldValue));
				}
				this.max = castMax;
			};
			EventManager.callEvent(new MumbleParameterMaxValueChangePreEvent(this, max), update, new MumbleParameterMaxValueChangePostEvent(this, oldMax));
		}
	}

	@SuppressWarnings("unchecked")
	private void check(Object value1, Object value2, String message) {
		Comparable<? super Number> value1Comparable = (Comparable<? super Number>) value1;
		if (value1Comparable.compareTo((Number) value2) > 0)
			throw new IllegalArgumentException(message);
	}

	/**
	 * Check if the given value is in the range associated to the range of this parameter.
	 * 
	 * @param value The value to check.
	 */
	private void checkRange(T value) {
		check(getMin(), value, String.format("The value %s should be in range [%s;%s]", value, getMin(), getMax()));
		check(value, getMax(), String.format("The value %s should be in range [%s;%s]", value, getMin(), getMax()));
	}
}
