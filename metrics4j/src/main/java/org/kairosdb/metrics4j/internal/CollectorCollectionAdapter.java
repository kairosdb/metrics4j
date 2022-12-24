package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
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

public class CollectorCollectionAdapter extends MetricsGatherer implements CollectorCollection
{
	private final Collector m_baseCollector;

	//todo change so we can keep track of how often they are used and get rid of old ones
	private final Map<TagKey, AgedMetricCollector> m_collectors;
	private final ArgKey m_argKey;
	private final Map<String, String> m_contextProperties;

	public CollectorCollectionAdapter(Collector baseCollector, ArgKey argKey,
			Map<String, String> contextProperties)
	{
		m_baseCollector = baseCollector;
		m_argKey = argKey;
		m_contextProperties = contextProperties;
		m_collectors = new ConcurrentHashMap<>();
	}

	private AgedMetricCollector newCollector(TagKey tagKey)
	{
		Collector clone = m_baseCollector.clone();
		clone.setContextProperties(m_contextProperties);
		return new AgedMetricCollector(clone);
	}

	void addCollector(TagKey tagKey, MetricCollector collector)
	{
		m_collectors.put(tagKey, new AgedMetricCollector(collector));
	}

	@Override
	public MetricCollector getCollector(TagKey tagKey)
	{
		AgedMetricCollector agedMetricCollector = m_collectors.computeIfAbsent(tagKey, (tk) -> newCollector(tk));
		agedMetricCollector.updateLastUsed();
		return agedMetricCollector.getMetricCollector();
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


}
