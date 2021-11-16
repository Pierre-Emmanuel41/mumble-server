package fr.pederobien.mumble.server.interfaces;

import fr.pederobien.mumble.common.impl.ParameterType;

public interface IParameter<T> extends Cloneable {

	/**
	 * @return The name of this parameter.
	 */
	public String getName();

	/**
	 * @return The value associated to this parameter.
	 */
	public T getValue();

	/**
	 * Set the value associated to this parameter. The value is first cast, then if the cast succeed, the value is set.
	 * 
	 * @param value The new parameter value.
	 */
	public void setValue(Object value);

	/**
	 * @return The default parameter value.
	 */
	public T getDefaultValue();

	/**
	 * @return The type of this parameter.
	 */
	public ParameterType<T> getType();

	/**
	 * @return The sound modifier attached to this parameter.
	 */
	ISoundModifier getSoundModifier();

	/**
	 * Clone this parameter. It creates a new parameter based on the properties of this parameter.
	 * 
	 * @return A new parameter.
	 */
	IParameter<T> clone();
}
