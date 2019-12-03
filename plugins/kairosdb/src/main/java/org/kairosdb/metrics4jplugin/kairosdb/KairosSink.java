package org.kairosdb.metrics4jplugin.kairosdb;

import lombok.Getter;
import lombok.Setter;
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
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.List;

public class KairosSink implements MetricSink, Closeable
{
	private Logger logger = LoggerFactory.getLogger(KairosSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();
	private Client m_client;

	@Setter
	private String hostUrl = "http://localhost";

	@Setter
	private String telnetHost = null;

	@Setter
	private int telnetPort = 4242;

	@Setter
	private Duration ttl = Duration.ofSeconds(0);

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

				long timeout = ttl.getSeconds();
				if (timeout != 0)
					sendMetric.addTtl((int)timeout);

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
		logger.info("Initializing Kairosdb client");
		if (telnetHost == null)
		{
			try
			{
				m_client = new HttpClient(hostUrl);
			}
			catch (MalformedURLException e)
			{
				logger.error("Malformed URL for Kairos client", e);
			}
		}
		else
		{
			try
			{
				m_client = new TelnetClientAdapter(new TelnetClient(telnetHost, telnetPort));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() throws IOException
	{
		m_client.close();
	}
}
