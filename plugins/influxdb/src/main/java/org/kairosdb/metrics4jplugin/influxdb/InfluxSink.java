package org.kairosdb.metrics4jplugin.influxdb;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class InfluxSink implements MetricSink, Closeable
{
	@Setter
	private String hostUrl = "http://localhost:8086/write";

	private CloseableHttpClient m_httpClient;
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

	private String escape(String in)
	{
		return in;
	}

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		for (FormattedMetric metric : metrics)
		{
			StringBuilder sb = new StringBuilder();

			sb.append(escape(metric.getSamples().get(0).getMetricName()));

			Map<String, String> tags = metric.getTags();
			for (String tagKey : tags.keySet())
			{
				sb.append(",").append(escape(tagKey)).append("=")
						.append(escape(tags.get(tagKey)));
			}

			Instant lastSample = Instant.now();
			for (FormattedMetric.Sample sample : metric.getSamples())
			{
				sb.append(" ").append(sample.getFieldName()).append("=")
						.append(escape(sample.getValue().getValueAsString()));

				lastSample = sample.getTime();
			}


			long time = lastSample.getEpochSecond();
			time *= 1000000000l; //convert to nanoseconds
			time += lastSample.getNano(); //the nanoseconds returned by inst.getNano() are the nanoseconds past the second so they need to be added to the epoch second
			sb.append(" ").append(time);

			sb.append("\n");
		}

		//todo send using http post
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}

	@Override
	public void init(MetricsContext context)
	{
		m_httpClient = HttpClients.createDefault();
	}

	@Override
	public void close() throws IOException
	{
		m_httpClient.close();
	}
}
