package fr.pederobien.mumble.server.impl.modifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import fr.pederobien.mumble.common.impl.ParameterType;
import fr.pederobien.mumble.server.event.ParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.ParameterValueChangePreEvent;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.ISoundModifier;
import fr.pederobien.utils.event.EventManager;

public class Parameter<T> implements IParameter<T> {
	protected static final Map<Class<?>, ParameterType<?>> PRIMITIVE_TYPES;
	protected static final Map<Class<?>, ParameterType<?>> RANGE_TYPES;

	static {
		PRIMITIVE_TYPES = new HashMap<Class<?>, ParameterType<?>>();
		PRIMITIVE_TYPES.put(Boolean.class, ParameterType.BOOLEAN);
		PRIMITIVE_TYPES.put(Character.class, ParameterType.CHAR);
		PRIMITIVE_TYPES.put(Byte.class, ParameterType.BYTE);
		PRIMITIVE_TYPES.put(Short.class, ParameterType.SHORT);
		PRIMITIVE_TYPES.put(Integer.class, ParameterType.INT);
		PRIMITIVE_TYPES.put(Long.class, ParameterType.LONG);
		PRIMITIVE_TYPES.put(Float.class, ParameterType.FLOAT);
		PRIMITIVE_TYPES.put(Double.class, ParameterType.DOUBLE);

		RANGE_TYPES = new HashMap<Class<?>, ParameterType<?>>(PRIMITIVE_TYPES);
		RANGE_TYPES.remove(Boolean.class);
		RANGE_TYPES.remove(Character.class);
	}

	/**
	 * Creates a new parameter based on the given parameters.
	 * 
	 * @param <T>           The type of this parameter.
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param defaultValue  the parameter default value.
	 * @param value         The parameter value.
	 * @return The created parameter initialized with the given parameters.
	 */
	public static <T> Parameter<T> of(ISoundModifier soundModifier, String name, T defaultValue, T value) {
		return new Parameter<T>(soundModifier, name, defaultValue, value);
	}

	/**
	 * Creates a new parameter based on the given parameters. The current value equals the default parameter value.
	 * 
	 * @param <T>           The type of this parameter.
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param defaultValue  the parameter default value.
	 * @return The created parameter initialized with the given parameters.
	 */
	public static <T> Parameter<T> of(ISoundModifier soundModifier, String name, T defaultValue) {
		return of(soundModifier, name, defaultValue, defaultValue);
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
	 * @return The created parameter initialized with the given parameters.
	 */
	public static <T> Parameter<T> fromType(ParameterType<T> type, String name, String defaultValue, String value) {
		return of(null, name, type.getValue(defaultValue), type.getValue(value));
	}

	private String name;
	private T value, defaultValue;
	private ParameterType<T> type;
	private ISoundModifier soundModifier;

	/**
	 * Creates a parameter with a name and a value.
	 * 
	 * @param soundModifier The sound modifier associated to this parameter.
	 * @param name          The parameter name.
	 * @param defaultValue  the default parameter value.
	 * @param value         The parameter value.
	 */
	@SuppressWarnings("unchecked")
	protected Parameter(ISoundModifier soundModifier, String name, T defaultValue, T value) {
		if ((type = (ParameterType<T>) PRIMITIVE_TYPES.get(value.getClass())) == null)
			throw new IllegalArgumentException("The type of the generic parameter must be a primitive type.");

		this.soundModifier = soundModifier;
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public T getValue() {
		return value;
	}

	@Override
	public void setValue(Object value) {
		if (this.value.equals(value))
			return;

		T oldValue = this.value;
		Runnable set = () -> this.value = type.cast(value);
		EventManager.callEvent(new ParameterValueChangePreEvent(this, getValue(), value), set, new ParameterValueChangePostEvent(this, oldValue));
	}

	@Override
	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public ParameterType<T> getType() {
		return type;
	}

	@Override
	public ISoundModifier getSoundModifier() {
		return soundModifier;
	}

	/**
	 * Set the sound modifier associated to this parameter.
	 * 
	 * @param soundModifier The new sound modifier.
	 */
	public void setSoundModifier(ISoundModifier soundModifier) {
		this.soundModifier = soundModifier;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("name=" + getName());
		joiner.add("value=" + getValue());
		joiner.add("defaultValue=" + getDefaultValue());
		joiner.add("type=" + getType());
		joiner.add("soundModifier=" + getSoundModifier().getName());
		return joiner.toString();
	}

	@Override
	public IParameter<T> clone() {
		return new Parameter<T>(getSoundModifier(), getName(), getDefaultValue(), getValue());
	}
}