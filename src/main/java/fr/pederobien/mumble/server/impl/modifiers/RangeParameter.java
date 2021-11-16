package fr.pederobien.mumble.server.impl.modifiers;

import java.util.StringJoiner;

import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.Range;

public class RangeParameter<T extends Number & Comparable<T>> extends Parameter<T> {
	private Range<T> range;

	/**
	 * Creates a range parameter. A range is associated to this parameter and when the value is changed the range validate or
	 * invalidate the modification.
	 * 
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param value         The parameter value.
	 * @param defaultValue  The default parameter value.
	 * @param range         The parameter range.
	 */
	public static <T extends Number & Comparable<T>> RangeParameter<T> of(ISoundModifier soundModifier, String name, T defaultValue, T value, Range<T> range) {
		return new RangeParameter<T>(soundModifier, name, defaultValue, value, range);
	}

	/**
	 * Creates a range parameter. A range is associated to this parameter and when the value is changed the range validate or
	 * invalidate the modification.
	 * 
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param defaultValue  The default parameter value.
	 * @param range         The parameter range.
	 */
	public static <T extends Number & Comparable<T>> RangeParameter<T> of(ISoundModifier soundModifier, String name, T defaultValue, Range<T> range) {
		return of(soundModifier, name, defaultValue, defaultValue, range);
	}

	/**
	 * Creates a range parameter. A range is associated to this parameter and when the value is changed the range validate or
	 * invalidate the modification.
	 * 
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param value         The parameter value.
	 * @param defaultValue  The default parameter value.
	 * @param range         The parameter range.
	 */
	protected RangeParameter(ISoundModifier soundModifier, String name, T defaultValue, T value, Range<T> range) {
		super(soundModifier, name, defaultValue, value);
		this.range = range;

		checkRange(defaultValue);
		checkRange(value);
	}

	@Override
	public void setValue(Object value) {
		checkRange(getType().cast(value));
		super.setValue(value);
	}

	/**
	 * @return The range associated to this parameter.
	 */
	public Range<T> getRange() {
		return range;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("name=" + getName());
		joiner.add("value=" + getValue());
		joiner.add("defaultValue=" + getDefaultValue());
		joiner.add("type=" + getType());
		joiner.add("range=" + getRange());
		return joiner.toString();
	}

	@Override
	public IParameter<T> clone() {
		return new RangeParameter<T>(getSoundModifier(), getName(), getDefaultValue(), getValue(), getRange());
	}

	private void checkRange(T value) {
		if (!range.contains(value))
			throw new IllegalArgumentException(String.format("The value %s should be in range %s", value, range.toString()));
	}
}
