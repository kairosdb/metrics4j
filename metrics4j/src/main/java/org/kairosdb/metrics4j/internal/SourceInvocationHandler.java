package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.configuration.MetricConfig;

import java.lang.reflect.Method;

public class SourceInvocationHandler implements SourceInvocationHandlerImpl
{
	private SourceInvocationHandlerImpl m_impl;

	public SourceInvocationHandler(MetricConfig config)
	{
		m_impl = new SourceInvocationHandlerRuntime(config);
	}

	public void setImplementation(SourceInvocationHandlerImpl impl)
	{
		m_impl = impl;
	}

	public SourceInvocationHandlerImpl getImplementation()
	{
		return m_impl;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		return m_impl.invoke(proxy, method, args);
	}

	@Override
	public void setCollector(MethodArgKey key, MetricCollector statsObject)
	{
		m_impl.setCollector(key, statsObject);
	}
}
