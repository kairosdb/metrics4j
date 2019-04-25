package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4JMetricSink implements MetricSink
{
	private Logger logger = LoggerFactory.getLogger(Slf4JMetricSink.class);

	public void reportMetrics(List<ReportedMetric> metrics)
	{
		for (ReportedMetric metric : metrics)
		{
			for (Map.Entry<String, MetricValue> field : metric.getFields().entrySet())
			{
				logger.info("metric={}.{}, time={}, value={}", metric.getMetricName(),
						field.getKey(), metric.getTime(), field.getValue().getValueAsString());
			}

		}
	}
}
