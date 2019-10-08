package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CollectorContainer
{
	private final MetricCollector m_collector;
	private final ArgKey m_argKey;
	private Formatter m_formatter;
	private List<SinkQueue> m_sinkQueueList;
	private Map<String, String> m_tags;

	public CollectorContainer(MetricCollector collector, ArgKey argKey)
	{
		m_collector = Objects.requireNonNull(collector);
		m_argKey = argKey;
		m_sinkQueueList = new ArrayList<>();
	}

	public CollectorContainer setFormatter(Formatter formatter)
	{
		m_formatter = formatter;
		return this;
	}

	public CollectorContainer addSinkQueue(List<SinkQueue> queue)
	{
		m_sinkQueueList.addAll(queue);
		return this;
	}

	public List<SinkQueue> getSinkQueueList()
	{
		return m_sinkQueueList;
	}

	private void formatAndSink(ReportedMetric metric)
	{
		if (m_formatter != null)
			m_formatter.formatReportedMetric(metric);

		for (SinkQueue sinkQueue : m_sinkQueueList)
		{
			sinkQueue.addMetric(metric);
		}
	}

	public void reportMetrics(Instant now)
	{
		m_collector.reportMetric(new MetricReporter()
		{
			@Override
			public void put(String field, MetricValue value)
			{
				ReportedMetric reportedMetric = new ReportedMetricImpl();
				reportedMetric.setTime(now)
						.setClassName(m_argKey.getClassName())
						.setMethodName(m_argKey.getMethodName())
						.setTags(m_tags)
						.setFieldName(field)
						.setValue(value);

				formatAndSink(reportedMetric);
			}

			@Override
			public void put(String field, MetricValue value, Instant time)
			{
				ReportedMetric reportedMetric = new ReportedMetricImpl();
				reportedMetric.setTime(time)
						.setClassName(m_argKey.getClassName())
						.setMethodName(m_argKey.getMethodName())
						.setTags(m_tags)
						.setFieldName(field)
						.setValue(value);

				formatAndSink(reportedMetric);
			}
		});

	}

	public void setTags(Map<String, String> tags)
	{
		m_tags = tags;
	}
}
