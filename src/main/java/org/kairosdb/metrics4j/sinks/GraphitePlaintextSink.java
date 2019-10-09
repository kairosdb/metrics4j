package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "sink")
public class GraphitePlaintextSink extends TextSocketSink
{
	private static final Logger logger = LoggerFactory.getLogger(GraphitePlaintextSink.class);

	@XmlAttribute(name = "include_tags", required = false)
	private boolean m_includeTags = true;


	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{
		logger.debug("Sending {} events to {}", metrics.size(),  m_host);

		for (ReportedMetric metric : metrics)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(metric.getMetricName());

			if (m_includeTags)
			{
				for (Map.Entry<String, String> tag : metric.getTags().entrySet())
				{
					sb.append(";").append(tag.getKey()).append("=").append(tag.getValue());
				}
			}

			sb.append(" ").append(metric.getValue().getValueAsString());

			sb.append(" ").append(metric.getTime().getEpochSecond());

			sendText(sb.toString());
		}
	}

	@Override
	public void init(MetricsContext context)
	{
		super.init(context);
	}
}
