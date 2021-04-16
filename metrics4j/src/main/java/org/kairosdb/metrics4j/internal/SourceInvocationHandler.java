package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.annotation.Help;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.configuration.ImplementationException;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceInvocationHandler implements InvocationHandler
{
	private static Logger log = LoggerFactory.getLogger(SourceInvocationHandler.class);
	public static final String COLLECTOR_PACKAGE = "org.kairosdb.metrics4j.collectors";

	private final Map<MethodArgKey, CollectorContext> m_statsMap = new ConcurrentHashMap<>();
	private final MetricConfig m_config;

	//ephemeral collectors should only be within this class

	public SourceInvocationHandler(MetricConfig config)
	{
		m_config = config;
	}

	private CollectorContext getCollectorContext(MethodArgKey key)
	{
		return m_statsMap.computeIfAbsent(key, (MethodArgKey k) ->
				lookupCollectorContext(k));
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		MethodArgKey key = new MethodArgKey(method, args);

		CollectorContext collectorContext = getCollectorContext(key);
		CollectorCollection collection = collectorContext.getCollection();

		TagKey tagKey = key.getTagKey(collectorContext.getTags());
		//TagKey tagKey = key.getTagKey(Collections.emptyMap());

		MetricCollector ret = collection.getCollector(tagKey);

		return ret;
	}

	public void setCollector(MethodArgKey key, MetricCollector statsObject)
	{
		Class<?> returnType = key.getMethod().getReturnType();

		if (!returnType.isAssignableFrom(statsObject.getClass()))
		{
			throw new IllegalArgumentException("The collectors object "+statsObject.getClass().getName()+
					" does not match return type for method "+key.getMethod().getName()+" which should be "+returnType.getName());
		}

		CollectorContext context = m_statsMap.get(key);
		if ((context == null) || (!(context.getCollection() instanceof CollectorCollectionAdapter)))
		{
			CollectorCollection collection = new CollectorCollectionAdapter(new DevNullCollector(), key);

			context = new TestingCollectorContext(collection);
			m_statsMap.put(key, context);
		}

		CollectorCollection collection = context.getCollection();

		((CollectorCollectionAdapter)collection).addCollector(key.getTagKey(context.getTags()), statsObject);
	}

	/**
	 Called once for each unique key.  Results are cached
	 @param key
	 @return
	 */
	private CollectorContext lookupCollectorContext(MethodArgKey key)
	{
		log.debug("Looking up collector for {}", key);

		if (m_config.isDisabled(key))
		{
			log.debug("Collector is disabled");
			return new DevNullCollectorContext();
		}

		Class<?> returnType = key.getMethod().getReturnType();
		//Validate return type is a generic collector
		String className = returnType.getName();
		if (!COLLECTOR_PACKAGE.equals(className.substring(0, className.lastIndexOf('.'))))
		{
			throw new ImplementationException("You have specified a return type on "+key.getClassName()+"."+key.getMethodName()+" that is not a generic collector interface as found in "+COLLECTOR_PACKAGE);
		}
		CollectorContext ret = null;

		Iterator<Collector> collectors = m_config.getContext().getCollectorsForKey(key).iterator();
		String helpText = "";

		Method method = key.getMethod();



		if (method.isAnnotationPresent(Help.class))
		{
			Help help = method.getAnnotation(Help.class);
			helpText = help.value();
		}

		while (collectors.hasNext())
		{
			Collector collector = collectors.next();

			/**
			 If the key matches exactly the collector then we error if it doesn't
			 match the return type
			 */
			if (returnType.isInstance(collector))
			{
				log.debug("Returning collector {}", collector);
				//Collector will be cloned before use in the adapter
				CollectorCollectionAdapter collectorCollection = new CollectorCollectionAdapter(collector, key);

				Map<String, String> tagsForKey = m_config.getTagsForKey(key);

				ret = m_config.getContext().assignCollector(key, collectorCollection, tagsForKey, m_config.getPropsForKey(key),
						m_config.getMetricNameForKey(key), helpText);

				break;
			}
			//todo else check for instance of CollectorCollection
			/*else
			{
				throw new ClassCastException("Unable to cast "+collector.getClass()+" to return type " + returnType.getName());
			}*/
		}

		if (ret == null)
		{
			log.info("Unable to find collector for "+key);
			ret = new DevNullCollectorContext();
		}

		return ret;
	}

}
