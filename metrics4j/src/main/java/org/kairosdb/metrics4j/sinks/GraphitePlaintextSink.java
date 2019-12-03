package org.kairosdb.metrics4j.sinks;

import lombok.Setter;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class GraphitePlaintextSink extends TextSocketSink
{
	private static final Logger logger = LoggerFactory.getLogger(GraphitePlaintextSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

	@Setter
	private boolean includeTags = true;

	public GraphitePlaintextSink(boolean includeTags)
	{
		super();
		this.includeTags = includeTags;
	}

	public GraphitePlaintextSink()
	{
		this(true);
	}

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		logger.debug("Sending {} events to {}", metrics.size(),  m_host);

		for (FormattedMetric metric : metrics)
		{
			for (FormattedMetric.Sample sample : metric.getSamples())
			{
				StringBuilder sb = new StringBuilder();
				sb.append(sample.getMetricName());

				if (includeTags)
				{
					for (Map.Entry<String, String> tag : metric.getTags().entrySet())
					{
						sb.append(";").append(tag.getKey()).append("=").append(tag.getValue());
					}
				}

				sb.append(" ").append(sample.getValue().getValueAsString());

				sb.append(" ").append(sample.getTime().getEpochSecond());

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

	@Override
	public void init(MetricsContext context)
	{
		super.init(context);
	}
}
