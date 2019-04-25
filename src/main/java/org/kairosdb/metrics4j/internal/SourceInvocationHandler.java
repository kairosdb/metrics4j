package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.ReportableMetric;
import org.kairosdb.metrics4j.collectors.SimpleCounter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceInvocationHandler implements InvocationHandler
{
	private final Map<ArgKey, StatsContainer> m_statsMap = new ConcurrentHashMap<>();

	public SourceInvocationHandler()
	{
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		ArgKey key = new ArgKey(method, args);

		StatsContainer ret = m_statsMap.computeIfAbsent(key, (ArgKey k) ->
				new StatsContainer(getStatsObject(k), method.getDeclaringClass().getName(), method.getName()));

		return ret.getStatsObject();
	}

	public void setStatsObject(ArgKey key, ReportableMetric statsObject)
	{
		Class<?> returnType = key.getMethod().getReturnType();

		if (!returnType.isAssignableFrom(statsObject.getClass()))
		{
			throw new IllegalArgumentException("The collectors object "+statsObject.getClass().getName()+
					" does not match return type for method "+key.getMethod().getName()+" which should be "+returnType.getName());
		}

		m_statsMap.put(key, new StatsContainer(statsObject, key.getMethod().getDeclaringClass().getName(), key.getMethod().getName()));
	}

	private Class<? extends ReportableMetric> lookupStatsClass(ArgKey key)
	{
		Class<?> returnType = key.getMethod().getReturnType();
		//todo implement lookup of configured object.
		//if nothing configured then return DevNullCollector

		return DevNullCollector.class;
	}

	private ReportableMetric getStatsObject(ArgKey k)
	{
		Class<?> returnType = k.getMethod().getReturnType();

		Class<? extends ReportableMetric> instanceType = lookupStatsClass(k);

		if (!ReportableMetric.class.isAssignableFrom(instanceType))
		{
			//todo put more information in exception
			throw new ClassCastException("Unable to cast configured object to ReportableStats");
		}

		if (!returnType.isAssignableFrom(instanceType))
		{
			throw new ClassCastException("Unable to cast to return type");
		}

		ReportableMetric ret = null;

		try {
			ret = instanceType.newInstance();
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		//todo Register collectors object

		return ret;
	}
}
