package org.kairosdb.metrics4jplugin.opentelemetry;

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import lombok.Getter;
import lombok.Setter;
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

public class OtelSink implements MetricSink, Closeable
{
	private static final Logger logger = LoggerFactory.getLogger(OtelSink.class);

	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

	@Setter
	private String endpoint = "http://localhost:4317";

	private OtlpGrpcMetricExporter m_exporter;

	private String escape(String in)
	{
		return in;
	}

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{

	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}

	@Override
	public void init(MetricsContext context)
	{
		OtlpGrpcMetricExporterBuilder builder = OtlpGrpcMetricExporter.builder();
		builder.setEndpoint(endpoint);


		m_exporter = builder.build();
	}

	@Override
	public void close() throws IOException
	{
		m_exporter.shutdown();
	}
}
