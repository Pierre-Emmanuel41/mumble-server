package fr.pederobien.mumble.server.impl.modifiers;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import fr.pederobien.mumble.common.impl.messages.v10.model.ParameterType;
import fr.pederobien.mumble.server.event.MumbleParameterValueChangePostEvent;
import fr.pederobien.mumble.server.event.MumbleParameterValueChangePreEvent;
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
	public static <T> Parameter<T> fromType(ParameterType<T> type, String name, Object defaultValue, Object value) {
		return of(null, name, type.cast(defaultValue), type.cast(value));
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
			throw new IllegalArgumentException(String.format("The type must be a primitive type (found %s instead).", value.getClass().getSimpleName()));

		this.soundModifier = soundModifier;
		this.name = name;
		this.defaultValue = defaultValue;
		this.value = value;
	}

	/**
	 * Protected constructor for clone method.
	 * 
	 * @param original The original parameter to clone.
	 */
	protected Parameter(Parameter<T> original) {
		this.soundModifier = original.getSoundModifier();
		this.name = original.getName();
		this.type = original.getType();
		this.value = original.getValue();
		this.defaultValue = original.getDefaultValue();
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
		T castValue = getType().cast(value);
		if (this.value.equals(castValue))
			return;

		if (!isAttached())
			this.value = castValue;
		else
			EventManager.callEvent(new MumbleParameterValueChangePreEvent(this, value), () -> setValue0(castValue));
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
	public Parameter<T> clone() {
		return new Parameter<T>(this);
	}

	/**
	 * @return True if the sound modifier associated to this parameter is attached to a channel, false otherwise.
	 */
	protected boolean isAttached() {
		return soundModifier != null && soundModifier.getChannel() != null;
	}

	/**
	 * Set the current value of this parameter.
	 * 
	 * @param value The current parameter value.
	 */
	protected void setValue0(T value) {
		T oldValue = this.value;
		this.value = value;
		EventManager.callEvent(new MumbleParameterValueChangePostEvent(this, oldValue));
	}
}
