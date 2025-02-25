package org.kairosdb.metrics4j.internal;

import lombok.ToString;
import org.kairosdb.metrics4j.CollectorNotification;
import org.kairosdb.metrics4j.FormatterNotification;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.SinkNotification;
import org.kairosdb.metrics4j.Snapshot;
import org.kairosdb.metrics4j.TriggerNotification;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.plugins.Plugin;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.triggers.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
public class MetricsContextImpl implements MetricsContext
{
	private static final Logger log = LoggerFactory.getLogger(MetricsContextImpl.class);

	private final ListComponentTracker<SinkQueue> m_sinks;
	private final ListComponentTracker<Collector> m_collectors;
	private final ListComponentTracker<AssignedFormatter> m_formatters;
	private final ComponentTracker<TriggerMetricCollection> m_triggers;

	public MetricsContextImpl()
	{
		m_sinks = new ListComponentTracker<>("sink");
		m_collectors = new ListComponentTracker<>("collector");
		m_formatters = new ListComponentTracker<>("formatter");
		m_triggers = new ComponentTracker<>("trigger");
	}

	public void addCollectorToPath(String name, List<String> path)
	{
		log.debug("Adding collector '{}' to path {}", name, path);
		m_collectors.addToPath(name, path);
	}

	public void addSinkToPath(String name, List<String> path)
	{
		log.debug("Adding sink '{}' to path {}", name, path);
		m_sinks.addToPath(name, path);
	}

	public void addFormatterToPath(String name, List<String> path)
	{
		log.debug("Adding formatter '{}' to path {}", name, path);
		m_formatters.addToPath(name, path);
	}

	public void addTriggerToPath(String name, List<String> path)
	{
		log.debug("Adding trigger '{}' to path {}", name, path);
		m_triggers.addToPath(name, path);
	}

	public void registerSink(String name, MetricSink sink)
	{
		log.debug("Registering sink {}", name);
		m_sinks.addComponent(name, new SinkQueue(sink, name));
	}

	public void registerCollector(String name, Collector collector)
	{
		log.debug("Registering collector {}", name);
		m_collectors.addComponent(name, collector);
	}

	public void registerFormatter(String name, Formatter formatter)
	{
		log.debug("Registering formatter {}", name);
		m_formatters.addComponent(name, new AssignedFormatter(name, formatter, "*"));
	}

	public void registerAssignedFormatter(String name, Formatter formatter, String sinkName)
	{
		log.debug("Registering assigned formatter {}", name);
		m_formatters.addComponent(name, new AssignedFormatter(name, formatter, sinkName));
	}

	public void registerTrigger(String name, Trigger trigger)
	{
		log.debug("Registering trigger {}", name);
		m_triggers.addComponent(name, new TriggerMetricCollection(trigger));
	}

	public void registerPlugin(String name, Plugin plugin)
	{
		log.debug("Registering plugin {}", name);
		//Nothing to do as registerStuff did it all for plain plugins
	}

	public MetricSink getSink(String name)
	{
		SinkQueue sinkQueue = m_sinks.getComponent(name);

		if (sinkQueue != null)
			return sinkQueue.getSink();
		else
			return null;
	}

	@Override
	public List<Collector> getCollectorsForKey(ArgKey key)
	{

		List<Collector> ret = m_collectors.getComponentsForKey(key);
		if (ret != null)
			return ret;
		else
			return Collections.emptyList();
	}

	private TriggerMetricCollection getTriggerForKey(ArgKey key)
	{
		TriggerMetricCollection triggerMetricCollection = m_triggers.getComponentForKey(key);
		if (triggerMetricCollection == null)
		{
			triggerMetricCollection = new TriggerMetricCollection(new NeverTrigger());
		}

		return triggerMetricCollection;
	}

	/**
	 Caller must clone Collector
	 @param name Name of collector
	 @return Collector
	 */
	public Collector getCollector(String name)
	{
		Collector collector = m_collectors.getComponent(name);
		return collector;
	}

	public Formatter getFormatter(String name)
	{
		return m_formatters.getComponent(name).getFormatter();
	}

	public Trigger getTrigger(String name)
	{
		return m_triggers.getComponent(name).getTrigger();
	}

	public void flushMetrics()
	{
		Instant now = Instant.now();
		m_triggers.getComponents().forEach(trigger -> trigger.reportMetrics(now));
	}


	/**
	 This assigns an instance of a collector to a trigger, associates a formatter
	 and places it in the appropriate sink queues.+
	 @param key ArgKey for collector
	 @param collectors Collectors to assign
	 @param collectorTags Collector tags
	 @param properties Properties for collector
	 @param metricName Metric name that is assigned in config
	 @param help Help text for collector
	 @return Collector context
	 */
	public CollectorContext assignCollector(ArgKey key, CollectorCollection collectors, Map<String, String> collectorTags,
			Map<String, String> properties, String metricName, String help)
	{
		//todo make sure assignment doesn't already exist
		CollectorContextImpl collectorContext = new CollectorContextImpl(collectors, key);

		if (metricName != null)
			collectorContext.setMetricName(metricName);

		collectorContext.setTags(collectorTags);
		collectorContext.setProps(properties);
		collectorContext.setHelp(help);

		List<AssignedFormatter> formatters = m_formatters.getComponentsForKey(key);
		List<SinkQueue> sinkQueues = m_sinks.getComponentsForKey(key);

		Map<String, Formatter> collectorFormatters = new HashMap<>();
		for (SinkQueue sinkQueue : sinkQueues)
		{
			String sinkName = sinkQueue.getSinkName();
			log.debug("Assigning metric {}.{} to sink {}", key.getClassName(), key.getMethodName(), sinkName);

			for (AssignedFormatter formatter : formatters)
			{
				if (formatter.getSinkRef().equals("*") || formatter.getSinkRef().equals(sinkName))
				{
					log.debug("Assigning metric {}.{} to formatter {}", key.getClassName(), key.getMethodName(), formatter.getName());
					collectorFormatters.put(sinkName, formatter.getFormatter());
					break;
				}
			}
		}

		collectorContext.setFormatters(collectorFormatters);

		collectorContext.addSinkQueue(sinkQueues);

		TriggerMetricCollection trigger = getTriggerForKey(key);
		trigger.addCollector(collectorContext);

		return collectorContext;
	}

	@Override
	public void assignSnapshot(ArgKey key, Snapshot snapshot)
	{
		TriggerMetricCollection trigger = getTriggerForKey(key);
		trigger.addSnapshot(snapshot);
	}

	@Override
	public void registerSinkNotification(SinkNotification notification)
	{
		m_sinks.addComponentListener((name, sink) -> notification.newSink(name, sink.getSink()));
	}

	@Override
	public void registerTriggerNotification(TriggerNotification notification)
	{
		m_triggers.addComponentListener((name, trigger) -> notification.newTrigger(name, trigger.getTrigger()));
	}

	@Override
	public void registerFormatterNotification(FormatterNotification notification)
	{
		m_formatters.addComponentListener((name, formatter) -> notification.newFormatter(name, formatter.getFormatter()));
	}

	@Override
	public void registerCollectorNotification(CollectorNotification notification)
	{
		m_collectors.addComponentListener((name, collector) -> notification.newCollector(name, collector));
	}
}
