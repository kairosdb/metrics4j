package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectorContainer
{
	private final Collector m_collector;
	private final ArgKey m_argKey;
	private Formatter m_formatter;
	private List<SinkQueue> m_sinkQueueList;
	private Map<String, String> m_tags;

	public CollectorContainer(Collector collector, ArgKey argKey)
	{
		m_collector = collector;
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

	public void reportMetrics(Instant now)
	{
		m_collector.reportMetric(new MetricReporter()
		{
			@Override
			public void put(String field, MetricValue value)
			{
				ReportedMetric reportedMetric = new ReportedMetricImpl();
				reportedMetric.setTime(now)
						.setClassName(m_argKey.getMethod().getDeclaringClass().getName())
						.setMethodName(m_argKey.getMethod().getName())
						.setTags(m_tags)
						.setFieldName(field)
						.setValue(value);

				m_formatter.formatReportedMetric(reportedMetric);

				for (SinkQueue sinkQueue : m_sinkQueueList)
				{
					sinkQueue.addMetric(reportedMetric);
				}
			}
		});

	}

	public void setTags(Map<String, String> tags)
	{
		m_tags = tags;
	}
}
