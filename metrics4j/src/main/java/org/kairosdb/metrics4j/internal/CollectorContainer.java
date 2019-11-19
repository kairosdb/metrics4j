package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.formatters.TemplateFormatter;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 Wraps a collector and associates it with a formatter and a list of sinks to send
 metrics to.  Also contains static tags to add to metric.
 */
public class CollectorContainer
{
	private final MetricCollector m_collector;
	private final ArgKey m_argKey;
	private Map<String, Formatter> m_formatters;
	private List<SinkQueue> m_sinkQueueList;
	private Map<String, String> m_tags;
	private Map<String, String> m_props;
	private String m_metricName;

	public CollectorContainer(MetricCollector collector, ArgKey argKey)
	{
		m_collector = Objects.requireNonNull(collector);
		m_argKey = argKey;
		m_sinkQueueList = new ArrayList<>();
	}

	public CollectorContainer setFormatters(Map<String, Formatter> formatters)
	{
		m_formatters = formatters;
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
		for (SinkQueue sinkQueue : m_sinkQueueList)
		{
			Formatter formatter = m_formatters.getOrDefault(sinkQueue.getSinkName(), sinkQueue.getSink().getDefaultFormatter());

			FormattedMetric formattedMetric = new FormattedMetric(metric);

			for (ReportedMetric.Sample sample : metric.getSamples())
			{
				String metricName = formatter.formatReportedMetric(metric, sample);
				formattedMetric.addSample(sample, metricName);
			}

			sinkQueue.addMetric(formattedMetric);
		}
	}

	public void reportMetrics(Instant now)
	{
		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setTime(now)
				.setMetricName(m_metricName)
				.setClassName(m_argKey.getClassName())
				.setMethodName(m_argKey.getMethodName())
				.setTags(m_tags)
				.setProps(m_props);

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

		formatAndSink(reportedMetric);
	}

	public void setTags(Map<String, String> tags)
	{
		m_tags = tags;
	}

	public void setMetricName(String metricName)
	{
		m_metricName = metricName;
	}


	public void setProps(Map<String, String> props)
	{
		m_props = props;
	}
}
