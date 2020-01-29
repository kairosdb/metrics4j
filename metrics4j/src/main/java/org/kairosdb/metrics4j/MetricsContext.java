package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.internal.CollectorCollection;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.triggers.Trigger;

import java.util.List;
import java.util.Map;

public interface MetricsContext
{
	//users can register for context changes where by they pass in a callback
	//for example the kairos internal sink will be looking for a connector
	//that will be registered by kairos upon startup

	//maybe a global context and a sink/trigger specific context

	void registerSinkNotification(SinkNotification notification);
	void registerTriggerNotification(TriggerNotification notification);
	void registerFormatterNotification(FormatterNotification notification);
	void registerCollectorNotification(CollectorNotification notification);

	List<Collector> getCollectorsForKey(ArgKey key);

	void registerTrigger(String name, Trigger trigger);

	void addTriggerToPath(String name, List<String> path);

	void registerFormatter(String name, Formatter formatter);

	void registerCollector(String name, Collector collector);

	void registerSink(String name, MetricSink sink);

	void addSinkToPath(String name, List<String> path);

	void assignCollector(ArgKey key, CollectorCollection collectors,
			Map<String, String> tags, Map<String, String> props, String metricName,
			String help);
}
