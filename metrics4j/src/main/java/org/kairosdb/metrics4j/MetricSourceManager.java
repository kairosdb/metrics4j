package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.annotation.Help;
import org.kairosdb.metrics4j.annotation.Reported;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.internal.DoubleLambdaCollectorAdaptor;
import org.kairosdb.metrics4j.internal.LambdaArgKey;
import org.kairosdb.metrics4j.internal.LongLambdaCollectorAdaptor;
import org.kairosdb.metrics4j.internal.MethodArgKey;
import org.kairosdb.metrics4j.internal.SourceInvocationHandler;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.internal.StaticCollectorCollection;
import org.kairosdb.metrics4j.internal.TagKey;
import org.kairosdb.metrics4j.internal.adapters.DoubleMethodCollectorAdapter;
import org.kairosdb.metrics4j.internal.adapters.DurationMethodCollectorAdapter;
import org.kairosdb.metrics4j.internal.adapters.LongMethodCollectorAdapter;
import org.kairosdb.metrics4j.internal.adapters.MethodSnapshotAdapter;
import org.kairosdb.metrics4j.internal.adapters.StringMethodCollectorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import java.util.function.LongSupplier;

import static java.util.Objects.requireNonNull;

/**
 Need to put methods to set various components programmatically.

 In kairos I want to set the reporter as a plugin to kairos that sends
 events using the event bus, but the configuration will reference this
 reporter by name in config even though no class will be defined in config.

 */
public class MetricSourceManager
{
	private static final Logger log = LoggerFactory.getLogger(MetricSourceManager.class);
	private static final Map<Class, SourceInvocationHandler> s_invocationMap = new ConcurrentHashMap<>();
	private static final Map<ArgKey, StaticCollectorCollection> s_staticCollectors = new ConcurrentHashMap<>();

	private static volatile MetricConfig s_metricConfig;

	/**
	 For testing purposes only, not to be used in production
	 @param config
	 */
	public static void setMetricConfig(MetricConfig config)
	{
		s_metricConfig = config;
		s_invocationMap.clear();
	}

	/**
	 For testing purposes, this clears out any config stored in the static variables
	 */
	public static void clearConfig()
	{
		s_metricConfig = null;
		s_invocationMap.clear();
	}

	public static MetricConfig getMetricConfig()
	{
		if (s_metricConfig == null)
		{
			try
			{
				s_metricConfig = MetricConfig.parseConfig("metrics4j.conf", "metrics4j.properties");
				s_metricConfig.runPostConfigInit();
			}
			catch (Exception e)
			{
				log.debug("Failed to load configuration", e);
				throw new RuntimeException(e);
			}
		}

		return s_metricConfig;
	}

	/*public static void registerMetricCollector(MetricCollector collector)
	{
		ArgKey key = new CustomArgKey(collector);

		getMetricConfig().getContext().assignCollector(key, collector, new HashMap<>(), new HashMap<>(), null);
	}*/

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

		//todo need to do some validation on tClass, makes ure all methods only take strings and are annotated with Keys

		requireNonNull(tClass, "Source class object cannot be null");
		InvocationHandler handler = s_invocationMap.computeIfAbsent(tClass, (klass) -> {
			MetricConfig metricConfig = getMetricConfig();
			if (metricConfig.isDumpMetrics())
			{
				String className = klass.getName();
				Method[] methods = klass.getMethods();
				for (Method method : methods)
				{
					String helpText = "";
					if (method.isAnnotationPresent(Help.class))
					{
						helpText = method.getAnnotation(Help.class).value();
					}
					metricConfig.addDumpSource(className+"."+method.getName(), helpText);
				}
			}
			return new SourceInvocationHandler(metricConfig);
		});

		//not sure if we should cache proxy instances or create new ones each time.
		Object proxyInstance = Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass},
				handler);

		return (T)proxyInstance;
	}

	/**
	 For registering a class to gather metrics from.  Methods annotated with
	 Reported will be called
	 @param o
	 @param tags
	 */
	public static void addSource(Object o, Map<String, String> tags)
	{
		//disable check is done in addSource call below
		MetricConfig metricConfig = getMetricConfig();
		MetricsContext context = metricConfig.getContext();

		List<Method> annotatedMethods = getAnnotatedMethods(o.getClass(), Reported.class);

		for (Method method : annotatedMethods)
		{
			Class<?> returnType = method.getReturnType();
			Reported annotation = method.getAnnotation(Reported.class);
			String helpText = annotation.help();
			String field = annotation.field();
			String className = o.getClass().getName();

			if (returnType == long.class || returnType == Long.class)
			{
				addSource(className, method.getName(), tags, helpText, new LongMethodCollectorAdapter(o, method, field));
			}
			else if (returnType == double.class || returnType == Double.class)
			{
				addSource(className, method.getName(), tags, helpText, new DoubleMethodCollectorAdapter(o, method, field));
			}
			else if (Duration.class.isAssignableFrom(returnType))
			{
				addSource(className, method.getName(), tags, helpText, new DurationMethodCollectorAdapter(o, method, field));
			}
			else if (CharSequence.class.isAssignableFrom(returnType))
			{
				addSource(className, method.getName(), tags, helpText, new StringMethodCollectorAdapter(o, method, field));
			}
			else
			{
				log.error("Unrecognized return type for "+className+" "+method.getName()+". Must be Long, Double, Duration or CharSequence.");
			}
		}

		annotatedMethods = getAnnotatedMethods(o.getClass(), org.kairosdb.metrics4j.annotation.Snapshot.class);

		for (Method method : annotatedMethods)
		{
			String className = o.getClass().getName();

			context.assignSnapshot(new LambdaArgKey(className, method.getName()), new MethodSnapshotAdapter(o, method));
		}
	}

	private static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation)
	{
		List<Method> retList = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods())
		{
			if (method.isAnnotationPresent(annotation) && !method.isSynthetic())
			{
				retList.add(method);
			}
		}
		return retList;
	}


	public static void removeSource(String className, String methodName, Map<String, String> tags)
	{
		ArgKey key = new LambdaArgKey(className, methodName);

		StaticCollectorCollection collection = s_staticCollectors.get(key);

		if (collection != null)
		{
			collection.removeCollector(buildTagKey(tags));
		}
	}

	private static TagKey buildTagKey(Map<String, String> tags)
	{
		//Need to make sure the tags are sorted
		Map<String, String> sortedTags = new TreeMap<>(tags);
		TagKey.Builder builder = TagKey.newBuilder();
		for (String tagKey : sortedTags.keySet())
		{
			builder.addTag(tagKey, sortedTags.get(tagKey));
		}

		return builder.build();
	}

	/**
	 In some cases it is more helpful to have the metrics code call into your code
	 to gather the metric or state of the system at the time of collection.  This
	 method allows you to artificially create a MetricCollector that will be called
	 when metrics are collected to be reported
	 @param className The class name to identify this collector for configuration purposes.
	 @param methodName The method name to identify this collector for configuration purposes.
	 @param tags Tags associated with this collector
	 @param help Help text for this collector
	 @param collector A instance that implements MetricCollector to be called when collecting metrics.
	 */
	public static void addSource(String className, String methodName, Map<String, String> tags, String help, MetricCollector collector)
	{
		requireNonNull(className, "className cannot be null");
		requireNonNull(methodName, "methodName cannot be null");
		requireNonNull(collector, "collector cannot be null");
		ArgKey key = new LambdaArgKey(className, methodName);
		MetricConfig metricConfig = getMetricConfig();

		if (metricConfig.isDumpMetrics())
		{
			metricConfig.addDumpSource(className+"."+methodName, help);
		}

		if (metricConfig.isDisabled(key))
			return;

		StaticCollectorCollection collection = s_staticCollectors.computeIfAbsent(key, (k) ->
		{
			Map<String, String> contextProperties = metricConfig.getPropsForKey(key);

			StaticCollectorCollection staticCollection = new StaticCollectorCollection(k, contextProperties);
			MetricsContext context = metricConfig.getContext();

			Map<String, String> configTags = metricConfig.getTagsForKey(key);
			if (tags != null)
				configTags.putAll(tags);

			context.assignCollector(key, staticCollection, configTags, contextProperties,
					metricConfig.getMetricNameForKey(key), help);

			return staticCollection;
		});

		collection.addCollector(buildTagKey(tags), collector);
	}


	/**
	 Returns properties set in configuration for the given class name and method name.
	 This is the key value data that shows up under the _props key in the configuration
	 file.
	 @param className
	 @param methodName
	 @return
	 */
	public static Map<String, String> getSourceProps(String className, String methodName)
	{
		requireNonNull(className, "className cannot be null");
		requireNonNull(methodName, "methodName cannot be null");
		ArgKey key = new LambdaArgKey(className, methodName);
		MetricConfig metricConfig = getMetricConfig();

		return metricConfig.getPropsForKey(key);
	}

	/**
	 A helper method that lets you pass a lambda function as the MetricCollector see {@link #addSource(String, String, Map, String, MetricCollector) addSource} method.
	 @param className The class name to identify this collector for configuration purposes.
	 @param methodName The method name to identify this collector for configuration purposes.
	 @param tags Tags associated with this collector
	 @param help Help text for this collector
	 @param supplier Lambda function that returns a long value.
	 */
	public static void addSource(String className, String methodName, Map<String, String> tags, String help, LongSupplier supplier)
	{
		addSource(className, methodName, tags, help, new LongLambdaCollectorAdaptor(supplier));
	}

	/**
	 A helper method that lets you pass a lambda function as the MetricCollector see {@link #addSource(String, String, Map, String, MetricCollector) addSource} method.
	 @param className The class name to identify this collector for configuration purposes.
	 @param methodName The method name to identify this collector for configuration purposes.
	 @param tags Tags associated with this collector
	 @param help Help text for this collector
	 @param supplier Lambda function that returns a double value.
	 */
	public static void addSource(String className, String methodName, Map<String, String> tags, String help, DoubleSupplier supplier)
	{
		addSource(className, methodName, tags, help, new DoubleLambdaCollectorAdaptor(supplier));
	}

	/**
		This method is provided for unit test purposes.  It lets you define
		a collectors object for a specific metric call.  See the unit tests
		in MetricSourceManagerTest to see how to use this method.
	*/
	public static <T> T setCollectorForSource(MetricCollector stats, Class<T> reporterClass)
	{
		MetricConfig metricConfig = getMetricConfig();

		SourceInvocationHandler handler = s_invocationMap.computeIfAbsent(reporterClass, (klass) -> new SourceInvocationHandler(metricConfig));

		Object proxyInstance = Proxy.newProxyInstance(reporterClass.getClassLoader(), new Class[]{reporterClass},
				(proxy, method, args) -> {
					handler.setCollector(new MethodArgKey(method, args), stats);
					return null;
				});

		return (T)proxyInstance;
	}

	public MetricsContext getMetricsContext()
	{
		return getMetricConfig().getContext();
	}

}
