package fr.pederobien.mumble.server.impl.modifiers;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

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
		for (Map.Entry<String, IParameter<?>> entry : original)
			parameters.put(entry.getValue().getName(), entry.getValue().clone());
	}

	@Override
	public Iterator<Map.Entry<String, IParameter<?>>> iterator() {
		return parameters.entrySet().iterator();
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
	public Map<String, IParameter<?>> getParameters() {
		return parameters;
	}

	@Override
	public int size() {
		return parameters.size();
	}

	@Override
	public void update(IParameterList parameterList) {
		for (Map.Entry<String, IParameter<?>> entry : parameterList) {
			IParameter<?> param = parameters.get(entry.getValue().getName());
			if (param == null)
				continue;
			param.setValue(entry.getValue().getValue());
		}
	}

	@Override
	public ParameterList clone() {
		return new ParameterList(this);
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
