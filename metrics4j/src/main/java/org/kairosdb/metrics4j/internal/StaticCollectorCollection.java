package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StaticCollectorCollection extends MetricsGatherer implements CollectorCollection
{
	private final Map<TagKey, AgedMetricCollector> m_collectors;
	private final ArgKey m_argKey;
	private final Map<String, String> m_contextProperties;

	public StaticCollectorCollection(ArgKey argKey, Map<String, String> contextProperties)
	{
		m_argKey = argKey;
		m_contextProperties = contextProperties;
		m_collectors = new ConcurrentHashMap<>();
	}

	/**
	 This method is only called by SourceInvocationHandler but it would never
	 call into a StaticCollectorCollection as they are not used for interface
	 invoked metrics.
	 @param tagKey TagKey for collector
	 @return MetricCollector
	 */
	@Override
	public MetricCollector getCollector(TagKey tagKey)
	{
		return m_collectors.get(tagKey).getMetricCollector();
	}

	public void removeCollector(TagKey tagKey)
	{
		m_collectors.remove(tagKey);
	}

	public void addCollector(TagKey tagKey, MetricCollector collector)
	{
		collector.setContextProperties(m_contextProperties);
		m_collectors.put(tagKey, new AgelessMetricCollector(collector));
	}

	@Override
	protected ArgKey getArgKey()
	{
		return m_argKey;
	}

	@Override
	protected Map<TagKey, AgedMetricCollector> getCollectors()
	{
		return m_collectors;
	}

	private class AgelessMetricCollector extends AgedMetricCollector
	{
		public AgelessMetricCollector(MetricCollector metricCollector)
		{
			super(metricCollector);
		}

		@Override
		public Duration getAge()
		{
			return Duration.ZERO;
		}
	}
}
