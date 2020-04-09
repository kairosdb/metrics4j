package org.kairosdb.metrics4j.internal;

import lombok.ToString;
import org.kairosdb.metrics4j.CollectorNotification;
import org.kairosdb.metrics4j.FormatterNotification;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.SinkNotification;
import org.kairosdb.metrics4j.TriggerNotification;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.triggers.Trigger;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
public class MetricsContextImpl implements MetricsContext
{
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
		m_collectors.addToPath(name, path);
	}

	public void addSinkToPath(String name, List<String> path)
	{
		m_sinks.addToPath(name, path);
	}

	public void addFormatterToPath(String name, List<String> path)
	{
		m_formatters.addToPath(name, path);
	}

	public void addTriggerToPath(String name, List<String> path)
	{
		m_triggers.addToPath(name, path);
	}

	public void registerSink(String name, MetricSink sink)
	{
		sink.init(this);
		m_sinks.addComponent(name, new SinkQueue(sink, name));
	}

	public void registerCollector(String name, Collector collector)
	{
		collector.init(this);
		m_collectors.addComponent(name, collector);
	}

	public void registerFormatter(String name, Formatter formatter)
	{
		formatter.init(this);
		m_formatters.addComponent(name, new AssignedFormatter(formatter, "*"));
	}

	public void registerAssignedFormatter(String name, Formatter formatter, String sinkName)
	{
		formatter.init(this);
		m_formatters.addComponent(name, new AssignedFormatter(formatter, sinkName));
	}

	public void registerTrigger(String name, Trigger trigger)
	{
		trigger.init(this);
		m_triggers.addComponent(name, new TriggerMetricCollection(trigger));
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

	public Collector getCollector(String name)
	{
		Collector collector = m_collectors.getComponent(name);
		//todo clone collector
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


	/**
	 This assigns an instance of a collector to a trigger, associates a formatter
	 and places it in the appropriate sink queues.+
	 @param key
	 @param collectors
	 @param collectorTags
	 */
	public void assignCollector(ArgKey key, CollectorCollection collectors, Map<String, String> collectorTags,
			Map<String, String> properties, String metricName, String help)
	{
		//todo make sure assignment doesn't already exist
		CollectorContainer collectorContainer = new CollectorContainer(collectors, key);

		if (metricName != null)
			collectorContainer.setMetricName(metricName);

		collectorContainer.setTags(collectorTags);
		collectorContainer.setProps(properties);
		collectorContainer.setHelp(help);

		List<AssignedFormatter> formatters = m_formatters.getComponentsForKey(key);
		List<SinkQueue> sinkQueues = m_sinks.getComponentsForKey(key);

		Map<String, Formatter> collectorFormatters = new HashMap<>();
		for (SinkQueue sinkQueue : sinkQueues)
		{
			String sinkName = sinkQueue.getSinkName();

			for (AssignedFormatter formatter : formatters)
			{
				if (formatter.getSinkRef().equals("*") || formatter.getSinkRef().equals(sinkName))
				{
					collectorFormatters.put(sinkName, formatter.getFormatter());
					break;
				}
			}
		}

		collectorContainer.setFormatters(collectorFormatters);

		collectorContainer.addSinkQueue(sinkQueues);

		TriggerMetricCollection trigger = getTriggerForKey(key);
		trigger.addCollector(collectorContainer);
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
