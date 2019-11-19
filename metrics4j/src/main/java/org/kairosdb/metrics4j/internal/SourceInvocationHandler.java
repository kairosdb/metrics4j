package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
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

	private final Map<MethodArgKey, MetricCollector> m_statsMap = new ConcurrentHashMap<>();
	private final MetricConfig m_config;

	public SourceInvocationHandler(MetricConfig config)
	{
		m_config = config;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		MethodArgKey key = new MethodArgKey(method, args);

		MetricCollector ret = m_statsMap.computeIfAbsent(key, (MethodArgKey k) ->
				lookupCollectorClass(k));

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

		m_statsMap.put(key, statsObject);
	}

	/**
	 Called once for each unique key.  Results are cached
	 @param key
	 @return
	 */
	private MetricCollector lookupCollectorClass(MethodArgKey key)
	{
		Class<?> returnType = key.getMethod().getReturnType();
		Collector ret = null;

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
				//Need to make a copy specific to this method arguments
				ret = collector.clone();

				Map<String, String> tagsForKey = m_config.getTagsForKey(key);
				tagsForKey.putAll(key.getTags());

				m_config.getContext().assignCollector(key, ret, tagsForKey, m_config.getPropsForKey(key),
						m_config.getMetricNameForKey(key));
			}
			/*else
			{
				throw new ClassCastException("Unable to cast "+collector.getClass()+" to return type " + returnType.getName());
			}*/
		}

		if (ret == null)
		{
			log.info("Unable to find collector for "+key);
			ret = new DevNullCollector();
		}

		return ret;
	}

}
