package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StaticCollectorCollection extends MetricsGatherer implements CollectorCollection
{
	private final Map<TagKey, MetricCollector> m_collectors;
	private final ArgKey m_argKey;

	public StaticCollectorCollection(ArgKey argKey)
	{
		m_argKey = argKey;
		m_collectors = new ConcurrentHashMap<>();
	}

	@Override
	public MetricCollector getCollector(TagKey tagKey)
	{
		return m_collectors.get(tagKey);
	}

	public void removeCollector(TagKey tagKey)
	{
		m_collectors.remove(tagKey);
	}

	public void addCollector(TagKey tagKey, MetricCollector collector)
	{
		m_collectors.put(tagKey, collector);
	}

	@Override
	protected ArgKey getArgKey()
	{
		return m_argKey;
	}

	@Override
	protected Map<TagKey, MetricCollector> getCollectors()
	{
		return m_collectors;
	}
}
