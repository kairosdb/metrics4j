package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "sink")
public class StatsDTCPSink extends TextSocketSink
{
	private static final Logger logger = LoggerFactory.getLogger(StatsDTCPSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();
	//need param to identify udp chunk size

	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{
		logger.debug("Sending {} events to {}", metrics.size(), m_host);
		//<bucket>:<value>|<type>|<sample rate>
		for (ReportedMetric metric : metrics)
		{
			for (ReportedMetric.Sample sample : metric.getSamples())
			{
				String type = metric.getProps().getOrDefault("statsd:type", "g");
				StringBuilder sb = new StringBuilder();

				sb.append(metric.getMetricName())
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
