package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.configuration.MissingReferenceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentTracker<T>
{
	public interface ComponentListener<T>
	{
		void newComponent(String name, T component);
	}

	protected final Map<String, T> m_components;
	private final Map<List<String>, T> m_mappedComponents;
	protected final String m_componentType;
	private final List<ComponentListener> m_listeners;

	public ComponentTracker(String componentType)
	{
		m_componentType = componentType;
		m_components = new HashMap<>();
		m_mappedComponents = new HashMap<>();
		m_listeners = new ArrayList<>();
	}

	public void addToPath(String name, List<String> path)
	{
		T component = m_components.get(name);
		if (component == null)
			throw new MissingReferenceException(m_componentType, name);

		m_mappedComponents.put(path, component);
	}

	public T getComponentForKey(ArgKey key)
	{
		T ret = null;
		List<String> configPath = key.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));
			ret = m_mappedComponents.get(searchPath);
			if (ret != null)
				break;
		}

		return ret;
	}

	public void addComponent(String name, T component)
	{
		//maybe a lock on this and addComponentListener
		m_components.put(name, component);
		for (ComponentListener listener : m_listeners)
		{
			listener.newComponent(name, component);
		}
	}

	public Collection<T> getComponents()
	{
		return m_components.values();
	}

	public T getComponent(String name)
	{
		return m_components.get(name);
	}

	public void addComponentListener(ComponentListener<T> listener)
	{
		for (String name : m_components.keySet())
		{
			listener.newComponent(name, m_components.get(name));
		}
		m_listeners.add(listener);
	}
}
