package org.kairosdb.metrics4jplugin.kairosdb;

import org.kairosdb.client.Client;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.TelnetClient;
import org.kairosdb.client.builder.Metric;
import org.kairosdb.client.builder.MetricBuilder;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@XmlRootElement(name = "sink")
public class KairosSink implements MetricSink, Closeable
{
	private Logger logger = LoggerFactory.getLogger(KairosSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();
	private Client m_client;

	@XmlAttribute(name = "host_url", required = false)
	private String m_hostUrl = "http://localhost";

	@XmlAttribute(name = "telnet_host", required = false)
	private String m_telnetHost = null;

	@XmlAttribute(name = "telnet_port", required = false)
	private int m_telnetPort = 4242;

	@XmlAttribute(name = "ttl", required = false)
	private int m_ttl = 0;

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		MetricBuilder builder = MetricBuilder.getInstance();

		for (FormattedMetric metric : metrics)
		{
			for (FormattedMetric.Sample sample : metric.getSamples())
			{
				Metric sendMetric = builder.addMetric(sample.getMetricName())
						.addTags(metric.getTags());

				if (m_ttl != 0)
					sendMetric.addTtl(m_ttl);

				//todo set ttl based on a property

				MetricValue value = sample.getValue();
				if (value instanceof LongValue)
					sendMetric.addDataPoint(sample.getTime().toEpochMilli(), ((LongValue)value).getValue());
				else if (value instanceof DoubleValue)
					sendMetric.addDataPoint(sample.getTime().toEpochMilli(), ((DoubleValue)value).getValue());
			}
		}

		m_client.pushMetrics(builder);
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}

	@Override
	public void init(MetricsContext context)
	{
		if (m_telnetHost != null)
		try
		{
			m_client = new HttpClient(m_hostUrl);
		}
		catch (MalformedURLException e)
		{
			logger.error("Malformed URL for Kairos client", e);
		}
	}

	@Override
	public void close() throws IOException
	{
		m_client.close();
	}
}
