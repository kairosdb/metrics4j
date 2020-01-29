package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectorCollectionAdapter implements CollectorCollection
{
	private final Collector m_baseCollector;

	//todo change so we can keep track of how often they are used and get rid of old ones
	private final Map<TagKey, MetricCollector> m_collectors;
	private final ArgKey m_argKey;

	public CollectorCollectionAdapter(Collector baseCollector, ArgKey argKey)
	{
		m_baseCollector = baseCollector;
		m_argKey = argKey;
		m_collectors = new HashMap<>();
	}

	private MetricCollector newCollector(TagKey tagKey)
	{
		return m_baseCollector.clone();
	}

	void addCollector(TagKey tagKey, MetricCollector collector)
	{
		m_collectors.put(tagKey, collector);
	}

	@Override
	public MetricCollector getCollector(TagKey tagKey)
	{
		return m_collectors.computeIfAbsent(tagKey, (tk) -> newCollector(tk));
	}

	@Override
	public Iterable<ReportedMetric> gatherMetrics(Instant now)
	{
		List<ReportedMetric> ret = new ArrayList<>();
		for (Map.Entry<TagKey, MetricCollector> entry : m_collectors.entrySet())
		{
			ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
			reportedMetric.setTime(now)
					.setClassName(m_argKey.getClassName())
					.setMethodName(m_argKey.getMethodName())
					.setTags(entry.getKey().getTags());

			entry.getValue().reportMetric(new MetricReporter()
			{
				@Override
				public void put(String field, MetricValue value)
				{
					reportedMetric.addSample(field, value);
				}

				@Override
				public void put(String field, MetricValue value, Instant time)
				{
					reportedMetric.addSample(field, value, time);
				}
			});

			ret.add(reportedMetric);
		}

		return ret;
	}
}
