package fr.pederobien.mumble.server.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface IParameterList extends Iterable<IParameter<?>> {

	/**
	 * Get the parameter associated to the given name.
	 * 
	 * @param name The parameter name.
	 * 
	 * @return An optional that contains the associated parameter if registered, an empty optional otherwise.
	 */
	Optional<IParameter<?>> get(String name);

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
