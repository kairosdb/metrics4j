package org.kairosdb.metrics4j.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReportingInvocationHandler implements InvocationHandler
{
	private final Map<ArgKey, StatsContainer> m_statsMap = new ConcurrentHashMap<>();

	public ReportingInvocationHandler()
	{
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		ArgKey key = new ArgKey(method, args);

		StatsContainer ret = m_statsMap.computeIfAbsent(key, (ArgKey k) ->
				new StatsContainer(getStatsObject(k), method.getDeclaringClass().getName(), method.getName()));

		return ret.getStatsObject();
	}

	public void setStatsObject(ArgKey key, Object statsObject)
	{
		Class<?> returnType = key.getMethod().getReturnType();

		if (!returnType.isAssignableFrom(statsObject.getClass()))
		{
			throw new IllegalArgumentException("The stats object "+statsObject.getClass().getName()+
					" does not match return type for method "+key.getMethod().getName()+" which should be "+returnType.getName());
		}

		m_statsMap.put(key, new StatsContainer(statsObject, key.getMethod().getDeclaringClass().getName(), key.getMethod().getName()));
	}

	private Object getStatsObject(ArgKey k)
	{
		Class<?> returnType = k.getMethod().getReturnType();
		Object ret = null;

		try {
			ret = returnType.newInstance();
		}
		catch (InstantiationException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		//todo Register stats object

		return ret;
	}
}
