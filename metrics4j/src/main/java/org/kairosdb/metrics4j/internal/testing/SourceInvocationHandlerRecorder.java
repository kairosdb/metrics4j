package org.kairosdb.metrics4j.internal.testing;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.internal.MethodArgKey;
import org.kairosdb.metrics4j.internal.SourceInvocationHandler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 This class is used to implement unit test recording of metrics
 */
public class SourceInvocationHandlerRecorder implements SourceInvocationHandler
{
	private Map<MethodArgKey, CollectorRecorder> m_counters = new ConcurrentHashMap<>();

	@Override
	public void setCollector(MethodArgKey key, MetricCollector statsObject)
	{
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		MethodArgKey key = new MethodArgKey(method, args);

		CollectorRecorder counter = m_counters.computeIfAbsent(key, (methodArgKey) -> new CollectorRecorder());

		return counter;
	}

	public CollectorRecorder getCollectorRecorder(MethodArgKey methodArgKey)
	{
		return m_counters.get(methodArgKey);
	}
}
