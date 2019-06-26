package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.internal.SourceInvocationHandler;
import org.kairosdb.metrics4j.collectors.ReportableMetric;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

/**
 Need to put methods to set various components programatically.

 In kairos I want to set the reporter as a plugin to kairos that sends
 events using the event bus, but the configuration will reference this
 reporter by name in config even though no class will be defined in config.

 */
public class MetricSourceManager
{
	private static Map<Class, SourceInvocationHandler> s_invocationMap = new ConcurrentHashMap<>();

	private static MetricConfig s_metricConfig;

	public static MetricConfig getMetricConfig()
	{
		if (s_metricConfig == null)
		{
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			InputStream configInputStream = contextClassLoader.getResourceAsStream("metrics4j.xml");
			try
			{
				s_metricConfig = MetricConfig.parseConfig(configInputStream);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		return s_metricConfig;
	}

	public static <T> T getSource(Class<T> tClass)
	{
		//Need to create invocationHandler for tClass using
		//invocation handler needs to setup for each method
		//in tClass an appropriate collectors object and registers the collectors
		//object in some central registry.

		//On some schedule the collectors objects in the registry and scrapped
		//for their data and that data is sent to any configured sinks endpoints (kairos, influx, etc..)
		//This all needs to be based on configuration that is dynamically loaded
		//so that it can change at runtime

		//Extra challenge - ability to have sinks endpoints get data at different
		//resolutions.  ex one endpoint gets data every minute and another endpoint
		//gets data every 10 min.  Challenge is in the collectors objects if they reset
		//any state or not


		InvocationHandler handler = s_invocationMap.computeIfAbsent(tClass, (klass) -> new SourceInvocationHandler(getMetricConfig()));

		//not sure if we should cache proxy instances or create new ones each time.
		Object proxyInstance = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass},
				handler);

		return (T)proxyInstance;
	}

	public static void export(Object o, Map<String, String> tags)
	{

	}

	public static void export(String name, Map<String, String> tags, LongSupplier supplier)
	{

	}

	public static void export(String name, Map<String, String> tags, DoubleSupplier supplier)
	{

	}

	/**
		This method is provided for unit test purposes.  It lets you define
		a collectors object for a specific metric call.  See the unit tests
		in ReporterFactoryTest to see how to use this method.
	*/
	public static <T> T setCollectorForSource(ReportableMetric stats, Class<T> reporterClass)
	{
		SourceInvocationHandler handler = s_invocationMap.computeIfAbsent(reporterClass, (klass) -> new SourceInvocationHandler(getMetricConfig()));

		Object proxyInstance = Proxy.newProxyInstance(reporterClass.getClassLoader(), new Class[]{reporterClass},
				(proxy, method, args) -> {
					handler.setCollector(new ArgKey(method, args), stats);
					return null;
				});

		return (T)proxyInstance;
	}

	public MetricsContext getMetricsContext()
	{
		return null;
	}

}
