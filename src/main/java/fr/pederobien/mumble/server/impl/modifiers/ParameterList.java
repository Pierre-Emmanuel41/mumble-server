package fr.pederobien.mumble.server.impl.modifiers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IParameterList;

public class ParameterList implements IParameterList {
	private Map<String, IParameter<?>> parameters;

	public ParameterList() {
		parameters = new LinkedHashMap<String, IParameter<?>>();
	}

	/**
	 * Private constructor for method clone.
	 * 
	 * @param original The original parameter list to clone.
	 */
	private ParameterList(ParameterList original) {
		parameters = new LinkedHashMap<String, IParameter<?>>();
		for (IParameter<?> parameter : original)
			parameters.put(parameter.getName(), parameter.clone());
	}

	@Override
	public Iterator<IParameter<?>> iterator() {
		return parameters.values().iterator();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Parameter<T> getParameter(String parameterName) {
		return (Parameter<T>) parameters.get(parameterName);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> void setParameterValue(String parameterName, T value) {
		IParameter<?> parameter = parameters.get(parameterName);
		if (parameter == null)
			return;
		((IParameter<T>) parameter).setValue(value);
	}

	@Override
	public int size() {
		return parameters.size();
	}

	@Override
	public void update(IParameterList parameterList) {
		for (IParameter<?> parameter : parameterList) {
			IParameter<?> param = parameters.get(parameter.getName());
			if (param == null)
				continue;
			param.setValue(parameter.getValue());
		}
	}

	@Override
	public ParameterList clone() {
		return new ParameterList(this);
	}

	@Override
	public Stream<IParameter<?>> stream() {
		return toList().stream();
	}

	@Override
	public List<IParameter<?>> toList() {
		return new ArrayList<IParameter<?>>(parameters.values());
	}

	/**
	 * Registers the given parameter in the list of parameters.
	 * 
	 * @param parameter The parameter to register.
	 */
	public void add(IParameter<?> parameter) {
		parameters.put(parameter.getName(), parameter);
	}

	/**
	 * Removes the parameter associated to the specified name.
	 * 
	 * @param parameterName The name of the parameter to remove.
	 */
	public void remove(String parameterName) {
		parameters.remove(parameterName);
	}
}
