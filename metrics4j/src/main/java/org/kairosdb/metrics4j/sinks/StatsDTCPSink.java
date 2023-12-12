package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.kairosdb.metrics4j.util.Properties.STATSD_TYPE;

public class StatsDTCPSink extends TextSocketSink
{
	private static final Logger logger = LoggerFactory.getLogger(StatsDTCPSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();
	//need param to identify udp chunk size

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		logger.debug("Sending {} events to {}", metrics.size(), m_host);
		//<bucket>:<value>|<type>|<sample rate>
		for (FormattedMetric metric : metrics)
		{
			for (FormattedMetric.Sample sample : metric.getSamples())
			{
				String type = metric.getProps().getOrDefault(STATSD_TYPE, "g");
				StringBuilder sb = new StringBuilder();

				sb.append(sample.getMetricName())
						.append(":")
						.append(sample.getValue().getValueAsString())
						.append("|")
						.append(type);

				sendText(sb.toString());
			}
		}

		flush();
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}
}
