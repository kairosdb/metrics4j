package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.ReportableMetric;
import org.kairosdb.metrics4j.configuration.MetricConfig;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceInvocationHandler implements InvocationHandler
{
	private final Map<ArgKey, StatsContainer> m_statsMap = new ConcurrentHashMap<>();
	private final MetricConfig m_config;

	public SourceInvocationHandler(MetricConfig config)
	{
		m_config = config;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		ArgKey key = new ArgKey(method, args);

		StatsContainer ret = m_statsMap.computeIfAbsent(key, (ArgKey k) ->
				new StatsContainer(lookupStatsClass(k), method.getDeclaringClass().getName(), method.getName()));

		return ret.getStatsObject();
	}

	public void setCollector(ArgKey key, ReportableMetric statsObject)
	{
		Class<?> returnType = key.getMethod().getReturnType();

		if (!returnType.isAssignableFrom(statsObject.getClass()))
		{
			throw new IllegalArgumentException("The collectors object "+statsObject.getClass().getName()+
					" does not match return type for method "+key.getMethod().getName()+" which should be "+returnType.getName());
		}

		m_statsMap.put(key, new StatsContainer(statsObject, key.getMethod().getDeclaringClass().getName(), key.getMethod().getName()));
	}

	private ReportableMetric lookupStatsClass(ArgKey key)
	{
		Class<?> returnType = key.getMethod().getReturnType();
		Collector ret = null;

		Collector collector = m_config.getCollectorForKey(key);

		if (collector != null)
		{
			if (!returnType.isAssignableFrom(collector.getClass()))
			{
				ret = collector;
			}
			else
			{
				throw new ClassCastException("Unable to cast "+collector.getClass()+" to return type " + returnType.getName());
			}

		}
		else
		{
			ret = new DevNullCollector();
		}
		//todo implement lookup of configured object.
		//if nothing configured then return DevNullCollector

		return ret;
	}

}
