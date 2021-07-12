package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CollectorContextImpl implements CollectorContext
{
	private static Logger log = LoggerFactory.getLogger(CollectorContext.class);

	private final CollectorCollection m_collector;
	private final ArgKey m_argKey;
	private Map<String, Formatter> m_formatters;
	private List<SinkQueue> m_sinkQueueList;
	private Map<String, String> m_tags;             //additional tags set in configuration
	private Map<String, String> m_props;
	private String m_metricName;
	private String m_help = "";

	public CollectorContextImpl(CollectorCollection collector, ArgKey argKey)
	{
		m_collector = Objects.requireNonNull(collector);
		m_argKey = argKey;
		m_sinkQueueList = new ArrayList<>();
	}

	public CollectorCollection getCollection()
	{
		return m_collector;
	}

	public CollectorContext setFormatters(Map<String, Formatter> formatters)
	{
		m_formatters = formatters;
		return this;
	}

	public CollectorContext addSinkQueue(List<SinkQueue> queue)
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

			if (formatter != null)
			{
				FormattedMetric formattedMetric = new FormattedMetric(metric, m_props, m_tags, m_help);

				for (ReportedMetric.Sample sample : metric.getSamples()) {
					String metricName = formatter.formatReportedMetric(metric, sample, m_metricName);

					log.debug("Reporting metric {} to sink {}", metricName, sinkQueue.getSinkName());
					formattedMetric.addSample(sample, metricName);
				}

				sinkQueue.addMetric(formattedMetric);
			}
			else {
				log.warn("No formatter configured for metric {}", metric.getMethodName());
			}
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

	public Map<String, String> getTags()
	{
		return m_tags;
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
