package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class StatsDTCPSink extends TextSocketSink
{
	private static final Logger logger = LoggerFactory.getLogger(StatsDTCPSink.class);
	//need param to identify udp chunk size

	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{
		//<bucket>:<value>|<type>|<sample rate>
		for (ReportedMetric metric : metrics)
		{
			String type = metric.getProps().getOrDefault("statsd:type", "g");
			logger.debug("Sending {} events to {}", metrics.size(), m_host);
			StringBuilder sb = new StringBuilder();

			sb.append(metric.getMetricName())
					.append(":")
					.append(metric.getValue().getValueAsString())
					.append("|")
					.append(type);

			sendText(sb.toString());
		}

		flush();
	}
}
