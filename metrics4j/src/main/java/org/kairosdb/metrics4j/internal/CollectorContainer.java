package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.CollectorCollection;
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

/**
 Wraps a collector and associates it with a formatter and a list of sinks to send
 metrics to.  Also contains static tags to add to metric.
 */
public class CollectorContainer
{
	private final CollectorCollection m_collector;
	private final ArgKey m_argKey;
	private Map<String, Formatter> m_formatters;
	private List<SinkQueue> m_sinkQueueList;
	private Map<String, String> m_tags;             //additional tags set in configuration
	private Map<String, String> m_props;
	private String m_metricName;
	private String m_help = "";

	public CollectorContainer(CollectorCollection collector, ArgKey argKey)
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
			Formatter formatter = m_formatters.computeIfAbsent(sinkQueue.getSinkName(), (sq) -> sinkQueue.getSink().getDefaultFormatter());

			FormattedMetric formattedMetric = new FormattedMetric(metric, m_props, m_tags, m_help);

			for (ReportedMetric.Sample sample : metric.getSamples())
			{
				String metricName = formatter.formatReportedMetric(metric, sample, m_metricName);
				formattedMetric.addSample(sample, metricName);
			}

			sinkQueue.addMetric(formattedMetric);
		}
	}

	public void reportMetrics(Instant now)
	{
		Iterable<ReportedMetric> reportedMetrics = m_collector.gatherMetrics(now);
		for (ReportedMetric metric : reportedMetrics)
		{
			formatAndSink(metric);
		}
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

	public String getHelp()
	{
		return m_help;
	}

	public void setHelp(String help)
	{
		m_help = help;
	}
}
