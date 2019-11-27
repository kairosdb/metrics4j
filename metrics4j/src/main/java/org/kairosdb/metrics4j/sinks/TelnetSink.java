package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "sink")
public class TelnetSink extends TextSocketSink
{
	private static final Logger logger = LoggerFactory.getLogger(TelnetSink.class);
	public static final String SECONDS = "SECONDS";
	public static final String MILLISECONDS = "MILLISECONDS";

	@XmlAttribute(name = "resolution", required = false)
	private String m_resolution = MILLISECONDS;

	private String m_command = "putm ";

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		logger.debug("Sending {} events via {}to {}", metrics.size(), m_command, m_host);

		for (FormattedMetric metric : metrics)
		{
			for (FormattedMetric.Sample sample : metric.getSamples())
			{
				StringBuilder sb = new StringBuilder();
				sb.append(m_command)
						.append(sample.getMetricName()).append(" ");

				if (m_resolution.equals(MILLISECONDS))
					sb.append(sample.getTime().toEpochMilli());
				else
					sb.append(sample.getTime().getEpochSecond());

				sb.append(" ").append(sample.getValue().getValueAsString());

				for (Map.Entry<String, String> tag : metric.getTags().entrySet())
				{
					sb.append(" ").append(tag.getKey()).append("=").append(tag.getValue());
				}

				sendText(sb.toString());
			}

		}

		flush();
	}

	@Override
	public void init(MetricsContext context)
	{
		super.init(context);
		if (m_resolution.toUpperCase().equals(SECONDS))
			m_command = "put ";
	}


}