package org.kairosdb.metrics4jplugin.influxdb;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class InfluxSink implements MetricSink, Closeable
{
	private static final Logger logger = LoggerFactory.getLogger(InfluxSink.class);
	@Setter
	private String hostUrl = "http://localhost:8086/write?db=mydb";

	//todo make db select a separate sink property

	private CloseableHttpClient m_httpClient;
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

	private String escape(String in)
	{
		return in;
	}

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		StringBuilder sb = new StringBuilder();
		for (FormattedMetric metric : metrics)
		{
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


			//todo add precision parameter
			long time = lastSample.getEpochSecond();
			time *= 1000000000l; //convert to nanoseconds
			time += lastSample.getNano(); //the nanoseconds returned by inst.getNano() are the nanoseconds past the second so they need to be added to the epoch second
			sb.append(" ").append(time);

			sb.append("\n");
		}

		HttpPost post = new HttpPost(hostUrl);
		post.setEntity(new ByteArrayEntity(sb.toString().getBytes()));

		try
		{
			m_httpClient.execute(post);
		}
		catch (IOException e)
		{
			logger.error("Unable to send metrics to Influx", e);
		}
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
