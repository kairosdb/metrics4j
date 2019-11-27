package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.CollectorCollection;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceInvocationHandler implements InvocationHandler
{
	private static Logger log = LoggerFactory.getLogger(SourceInvocationHandler.class);

	private final Map<MethodArgKey, CollectorCollection> m_statsMap = new ConcurrentHashMap<>();
	private final MetricConfig m_config;

	//ephemeral collectors should only be within this class

	public SourceInvocationHandler(MetricConfig config)
	{
		m_config = config;
	}

	private CollectorCollection getCollectorCollection(MethodArgKey key)
	{
		return m_statsMap.computeIfAbsent(key, (MethodArgKey k) ->
				lookupCollectorClass(k));
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		MethodArgKey key = new MethodArgKey(method, args);

		TagKey tagKey = key.getTagKey();

		CollectorCollection collection = getCollectorCollection(key);

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

		CollectorCollection collection = m_statsMap.get(key);
		if ((collection == null) || (!(collection instanceof CollectorCollectionAdapter)))
		{
			collection = new CollectorCollectionAdapter(new DevNullCollector(), key);
			m_statsMap.put(key, collection);
		}

		((CollectorCollectionAdapter)collection).addCollector(key.getTagKey(), statsObject);
	}

	/**
	 Called once for each unique key.  Results are cached
	 @param key
	 @return
	 */
	private CollectorCollection lookupCollectorClass(MethodArgKey key)
	{
		Class<?> returnType = key.getMethod().getReturnType();
		CollectorCollection ret = null;

		Iterator<Collector> collectors = m_config.getContext().getCollectorsForKey(key).iterator();

		while (collectors.hasNext())
		{
			Collector collector = collectors.next();

			/**
			 If the key matches exactly the collector then we error if it doesn't
			 match the return type
			 */
			if (returnType.isInstance(collector))
			{
				//Collector will be cloned before use in the adapter
				ret = new CollectorCollectionAdapter(collector, key);

				Map<String, String> tagsForKey = m_config.getTagsForKey(key);

				m_config.getContext().assignCollector(key, ret, tagsForKey, m_config.getPropsForKey(key),
						m_config.getMetricNameForKey(key));
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
			ret = new DevNullCollectorCollection();
		}

		return ret;
	}

}