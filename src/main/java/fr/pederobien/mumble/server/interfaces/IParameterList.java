package fr.pederobien.mumble.server.interfaces;

import java.util.List;
import java.util.stream.Stream;

public interface IParameterList extends Iterable<IParameter<?>> {

	/**
	 * Get the parameter associated to the given name.
	 * 
	 * @param <T>           The type of the parameter to return
	 * @param parameterName The parameter name.
	 * 
	 * @return The parameter associated to the name if registered.
	 */
	<T> IParameter<T> getParameter(String parameterName);

	/**
	 * Set the value of a parameter.
	 * 
	 * @param <T>           The type of the parameter.
	 * @param parameterName The parameter name.
	 * @param value         The new parameter value.
	 */
	<T> void setParameterValue(String parameterName, T value);

	/**
	 * @return The number of registered parameters.
	 */
	int size();

	/**
	 * Update the value of each parameter contains in this parameter list and the specified list.
	 * 
	 * @param parameterList The list that contains parameter to update.
	 */
	void update(IParameterList parameterList);

	/**
	 * Clone this parameter list. It creates a new parameter list based on the properties of this parameter list.
	 * 
	 * @return A new parameter list.
	 */
	IParameterList clone();

	/**
	 * @return a sequential {@code Stream} over the elements in this collection.
	 */
	Stream<IParameter<?>> stream();

	/**
	 * @return A copy of the underlying list.
	 */
	List<IParameter<?>> toList();
}
