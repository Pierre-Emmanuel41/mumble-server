package fr.pederobien.mumble.server.impl.modifiers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import fr.pederobien.mumble.common.impl.messages.v10.model.ParameterInfo.FullParameterInfo;
import fr.pederobien.mumble.server.interfaces.IParameter;
import fr.pederobien.mumble.server.interfaces.IParameterList;
import fr.pederobien.mumble.server.interfaces.IRangeParameter;

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
	public Optional<IParameter<?>> get(String name) {
		return Optional.ofNullable(parameters.get(name));
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

			if (param instanceof IRangeParameter<?> && parameter instanceof IRangeParameter<?>) {
				IRangeParameter<?> rangeParam = (IRangeParameter<?>) param;
				IRangeParameter<?> rangeParameter = (IRangeParameter<?>) parameter;
				rangeParam.setMin(rangeParameter.getMin());
				rangeParam.setMax(rangeParameter.getMax());
			}

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
	 * Registers the given parameter in the list of parameters.
	 * 
	 * @param parameter The parameter to register.
	 */
	public void add(FullParameterInfo info) {
		IParameter<?> parameter;
		if (!info.isRange()) {
			parameter = Parameter.fromType(info.getType(), info.getName(), info.getDefaultValue(), info.getValue());
		} else
			parameter = RangeParameter.fromType(info.getType(), info.getName(), info.getDefaultValue(), info.getValue(), info.getMinValue(), info.getMaxValue());

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
