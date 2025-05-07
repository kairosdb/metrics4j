package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 This class is used to implement unit test recording of metrics
 */
public class SourceInvocationHandlerRecorder implements SourceInvocationHandlerImpl
{
	private Map<MethodArgKey, RecordCounter> m_counters = new ConcurrentHashMap<>();

	@Override
	public void setCollector(MethodArgKey key, MetricCollector statsObject)
	{
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		MethodArgKey key = new MethodArgKey(method, args);

		RecordCounter counter = m_counters.computeIfAbsent(key, (methodArgKey) -> new RecordCounter());

		return counter;
	}
}
