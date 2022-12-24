package org.kairosdb.metrics4j.internal.adapters;

import org.kairosdb.metrics4j.collectors.MetricCollector;

import java.lang.reflect.Method;
import java.util.Map;

public abstract class MethodCollectorAdapter implements MetricCollector
{
	protected final Object m_object;
	protected final Method m_method;
	protected final String m_field;

	public MethodCollectorAdapter(Object object, Method method, String fieldName)
	{
		m_object = object;
		m_method = method;
		m_field = fieldName != null ? fieldName : "value";
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
