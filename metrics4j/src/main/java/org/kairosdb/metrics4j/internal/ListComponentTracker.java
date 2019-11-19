package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.configuration.MissingReferenceException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListComponentTracker<T> extends ComponentTracker<T>
{
	private final Map<List<String>, List<T>> m_mappedComponents;

	public ListComponentTracker(String componentType)
	{
		super(componentType);
		m_mappedComponents = new HashMap<>();
	}

	public void addToPath(String name, List<String> path)
	{
		T component = m_components.get(name);
		if (component == null)
			throw new MissingReferenceException(m_componentType, name);

		List<T> componentList = m_mappedComponents.computeIfAbsent(path, (k) -> new ArrayList<>());
		componentList.add(component);
	}

	public List<T> getComponentsForKey(ArgKey key)
	{
		List<T> ret = new ArrayList<>();
		List<String> configPath = key.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));
			List<T> objects = m_mappedComponents.get(searchPath);
			if (objects != null)
				ret.addAll(objects);
		}

		return ret;
	}
}
