package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MetricsGatherer implements CollectorCollection
{
	protected abstract ArgKey getArgKey();
	protected abstract Map<TagKey, MetricCollector> getCollectors();

	@Override
	public Iterable<ReportedMetric> gatherMetrics(Instant now)
	{
		ArgKey argKey = getArgKey();
		Map<TagKey, MetricCollector> collectors = getCollectors();

		List<ReportedMetric> ret = new ArrayList<>();
		for (Map.Entry<TagKey, MetricCollector> entry : collectors.entrySet())
		{
			ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
			reportedMetric.setTime(now)
					.setClassName(argKey.getClassName())
					.setMethodName(argKey.getMethodName())
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
