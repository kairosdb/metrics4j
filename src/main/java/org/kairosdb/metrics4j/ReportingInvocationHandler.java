package org.kairosdb.metrics4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReportingInvocationHandler implements InvocationHandler
{
	Map<ArgKey, Object> statsMap = new ConcurrentHashMap<>();

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		ArgKey key = new ArgKey(method, args);

		Object ret = statsMap.computeIfAbsent(key, (ArgKey k) -> getStatsObject(k));

		return ret;
	}

	public void setStatsObject(ArgKey key, Object statsObject)
	{
		Class<?> returnType = key.getMethod().getReturnType();

		if (!returnType.isAssignableFrom(statsObject.getClass()))
		{
			throw new IllegalArgumentException("The stats object "+statsObject.getClass().getName()+
					" does not match return type for method "+key.getMethod().getName()+" which should be "+returnType.getName());
		}

		statsMap.put(key, statsObject);
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
