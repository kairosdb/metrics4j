package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.ReportableMetric;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SourceInvocationHandler implements InvocationHandler
{
	private static Logger log = LoggerFactory.getLogger(SourceInvocationHandler.class);

	private final Map<ArgKey, ReportableMetric> m_statsMap = new ConcurrentHashMap<>();
	private final MetricConfig m_config;

	public SourceInvocationHandler(MetricConfig config)
	{
		m_config = config;
	}

	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
	{
		ArgKey key = new ArgKey(method, args);

		ReportableMetric ret = m_statsMap.computeIfAbsent(key, (ArgKey k) ->
				lookupCollectorClass(k));

		return ret;
	}

	public void setCollector(ArgKey key, ReportableMetric statsObject)
	{
		Class<?> returnType = key.getMethod().getReturnType();

		if (!returnType.isAssignableFrom(statsObject.getClass()))
		{
			throw new IllegalArgumentException("The collectors object "+statsObject.getClass().getName()+
					" does not match return type for method "+key.getMethod().getName()+" which should be "+returnType.getName());
		}

		m_statsMap.put(key, statsObject);
	}

	private ReportableMetric lookupCollectorClass(ArgKey key)
	{
		Class<?> returnType = key.getMethod().getReturnType();
		Collector ret = null;

		Iterator<Collector> collectors = m_config.getCollectorsForKey(key);

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

				//associate collector with
				CollectorContainer collectorContainer = new CollectorContainer(ret, key);

				collectorContainer.setTags(key.getTags());

				Formatter formatter = m_config.getFormatterForKey(key);
				if (formatter != null)
					collectorContainer.setFormatter(formatter);

				List<SinkQueue> sinkQueues = m_config.getSinkQueues(key);
				collectorContainer.addSinkQueue(sinkQueues);

				TriggerMetricCollection trigger = m_config.getTriggerForKey(key);
				trigger.addCollector(collectorContainer);
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
