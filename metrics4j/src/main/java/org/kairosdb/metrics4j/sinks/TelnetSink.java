package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static org.kairosdb.metrics4j.sinks.TelnetSink.Resolution.MILLISECONDS;
import static org.kairosdb.metrics4j.sinks.TelnetSink.Resolution.SECONDS;

public class TelnetSink extends TextSocketSink
{
	public enum Resolution
	{
		SECONDS,
		MILLISECONDS
	}

	private static final Logger logger = LoggerFactory.getLogger(TelnetSink.class);

	private Resolution resolution = MILLISECONDS;

	private String m_command = "putm ";


	public TelnetSink()
	{
		this(MILLISECONDS);
	}

	public TelnetSink(Resolution resolution)
	{
		setResolution(resolution);
	}

	public void setResolution(Resolution resolution)
	{
		this.resolution = resolution;
		if (resolution == SECONDS)
			m_command = "put ";
		else
			m_command = "putm ";
	}

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

				if (resolution.equals(MILLISECONDS))
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
	}


}
