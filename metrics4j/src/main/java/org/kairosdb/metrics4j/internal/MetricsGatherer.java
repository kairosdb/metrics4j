package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class MetricsGatherer implements CollectorCollection
{
	private static final Logger log = LoggerFactory.getLogger(MetricsGatherer.class);

	protected abstract ArgKey getArgKey();
	protected abstract Map<TagKey, AgedMetricCollector> getCollectors();

	@Override
	public Iterable<ReportedMetric> gatherMetrics(Instant now)
	{
		ArgKey argKey = getArgKey();
		Map<TagKey, AgedMetricCollector> collectors = getCollectors();

		List<ReportedMetric> ret = new ArrayList<>();
		Iterator<Map.Entry<TagKey, AgedMetricCollector>> collectorIterator = collectors.entrySet().iterator();
		while (collectorIterator.hasNext())
		{
			Map.Entry<TagKey, AgedMetricCollector> entry = collectorIterator.next();

			ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
			reportedMetric.setTime(now)
					.setClassName(argKey.getClassName())
					.setMethodName(argKey.getMethodName())
					.setTags(entry.getKey().getTags());

			MetricReporter reporter = new MetricReporter()
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
			};

			AgedMetricCollector agedMetricCollector = entry.getValue();
			agedMetricCollector.getMetricCollector().reportMetric(reporter);

			if (reportedMetric.getSamples().size() == 0)
			{
				if (agedMetricCollector.getAge().getSeconds() > 600)
				{
					log.debug("Removing collector for {} {} - tag key: {}", argKey.getClassName(), argKey.getMethodName(), entry.getKey());
					collectorIterator.remove();
				}
			}
			else
			{
				agedMetricCollector.updateLastUsed();
			}

			ret.add(reportedMetric);
		}

		return ret;
	}

	protected static class AgedMetricCollector
	{
		private final MetricCollector m_metricCollector;
		private long m_lastUsed;

		public AgedMetricCollector(MetricCollector metricCollector)
		{
			m_metricCollector = metricCollector;
			m_lastUsed = System.nanoTime();
		}

		public MetricCollector getMetricCollector()
		{
			return m_metricCollector;
		}

		public void updateLastUsed()
		{
			m_lastUsed = System.nanoTime();
		}

		public Duration getAge()
		{
			return Duration.ofNanos(System.nanoTime() - m_lastUsed);
		}
	}

}
