package fr.pederobien.mumble.server.interfaces;

import java.util.List;

public interface IParameterList extends Iterable<IParameter<?>> {

	/**
	 * Get the parameter associated to the given name.
	 * 
	 * @param <T>           The type of the parameter to return
	 * @param parameterName The parameter name.
	 * 
	 * @return The parameter associated to the name if registered.
	 */
	public <T> IParameter<T> getParameter(String parameterName);

	/**
	 * Set the value of a parameter.
	 * 
	 * @param <T>           The type of the parameter.
	 * @param parameterName The parameter name.
	 * @param value         The new parameter value.
	 */
	public <T> void setParameterValue(String parameterName, T value);

	/**
	 * @return The underlying list of the registered parameters.
	 */
	public List<IParameter<?>> getParameters();

	/**
	 * @return The number of registered parameters.
	 */
	public int size();

	/**
	 * Clone this parameter list. It creates a new parameter list based on the properties of this parameter list.
	 * 
	 * @return A new parameter list.
	 */
	IParameterList clone();
}
