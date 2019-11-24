package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.CollectorCollection;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.Collections;

public class StaticCollectorCollection implements CollectorCollection
{
	private final ArgKey m_argKey;
	private final MetricCollector m_collector;

	public StaticCollectorCollection(ArgKey argKey, MetricCollector collector)
	{
		m_argKey = argKey;
		m_collector = collector;
	}

	@Override
	public MetricCollector getCollector(TagKey tagKey)
	{
		return m_collector;
	}

	@Override
	public Iterable<ReportedMetric> gatherMetrics(Instant now)
	{
		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setTime(now)
				.setClassName(m_argKey.getClassName())
				.setMethodName(m_argKey.getMethodName());

		m_collector.reportMetric(new MetricReporter()
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

		return Collections.singletonList(reportedMetric);
	}
}
