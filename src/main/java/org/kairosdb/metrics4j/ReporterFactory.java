package org.kairosdb.metrics4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReporterFactory
{
	private static Map<Class, ReportingInvocationHandler> s_invocationMap = new ConcurrentHashMap<>();

	public static <T> T getReporter(Class<T> tClass)
	{
		//Need to create invocationHandler for tClass using
		//invocation handler needs to setup for each method
		//in tClass an appropriate stats object and registers the stats
		//object in some central registry.

		//On some schedule the stats objects in the registry and scrapped
		//for their data and that data is sent to any configured reporter endpoints (kairos, influx, etc..)
		//This all needs to be based on configuration that is dynamically loaded
		//so that it can change at runtime

		//Extra challenge - ability to have reporter endpoints get data at different
		//resolutions.  ex one endpoint gets data every minute and another endpoint
		//gets data every 10 min.  Challenge is in the stats objects if they reset
		//any state or not

		//Need to make the ReporterFactory return mocked objects for testing
		//Would like unit tests be able to verify that a metric was sent.
		//Maybe an env variable will determine if objects returned are mocks

		InvocationHandler handler = s_invocationMap.computeIfAbsent(tClass, (klass) -> new ReportingInvocationHandler());

		//not sure if we should cache proxy instances or create new ones each time.
		Object proxyInstance = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass},
				handler);

		return (T)proxyInstance;
	}

	public static <T> T setStatsForMetric(Object stats, Class<T> reporterClass)
	{
		ReportingInvocationHandler handler = s_invocationMap.computeIfAbsent(reporterClass, (klass) -> new ReportingInvocationHandler());

		Object proxyInstance = Proxy.newProxyInstance(reporterClass.getClassLoader(), new Class[]{reporterClass},
				(proxy, method, args) -> {
					handler.setStatsObject(new ArgKey(method, args), stats);
					return null;
				});

		return (T)proxyInstance;
	}

}
