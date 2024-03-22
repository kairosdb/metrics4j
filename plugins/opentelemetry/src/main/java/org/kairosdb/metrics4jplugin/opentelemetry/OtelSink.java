package org.kairosdb.metrics4jplugin.opentelemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.metrics.internal.data.M4jFactory;
import io.opentelemetry.sdk.resources.Resource;
import lombok.Setter;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.reporting.SummaryContext;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.CHRONO_UNIT_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_GAUGE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_SUMMARY_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
import static org.kairosdb.metrics4j.reporting.MetricValue.TYPE_DOUBLE;
import static org.kairosdb.metrics4j.reporting.MetricValue.TYPE_LONG;

public class OtelSink implements MetricSink, Closeable
{
	private static final Logger logger = LoggerFactory.getLogger(OtelSink.class);

	private static final Formatter DEFAULT_FORMATTER = new OtelFormatter();
	public static final String OTEL_UNIT = "otel_unit";
	public static final String OTEL_IS_MONOTONIC = "otel_is_monotonic";
	private static final List<Double> BOUNDARIES = Arrays.asList(0.0, 1.0);

	@Setter
	private String endpoint = "http://localhost:4317";

	@Setter
	private String name = "metrics4j";

	private InstrumentationScopeInfo m_scopeInfo;

	private MetricExporter m_exporter;

	private String escape(String in)
	{
		return in;
	}

	private class SingleSample
	{
		private final String m_name;
		private final MetricDataType m_type;
		private final PointData m_pointData;


		private SingleSample(String name, MetricDataType type, PointData pointData)
		{
			m_name = name;
			m_type = type;
			m_pointData = pointData;
		}

		public String getName()
		{
			return m_name;
		}

		public MetricDataType getType()
		{
			return m_type;
		}

		public PointData getPointData()
		{
			return m_pointData;
		}
	}

	private String getChronoUnit(String chronoUnit, String defaultIfNull)
	{
		String ret = defaultIfNull;
		if (chronoUnit != null)
		{
			ChronoUnit unit = ChronoUnit.valueOf(chronoUnit);
			switch (unit)
			{
				case NANOS:
					ret = "ns";
					break;
				case MICROS:
					ret = "us";
					break;
				case MILLIS:
					ret = "ms";
					break;
				case SECONDS:
					ret = "s";
					break;
				case MINUTES:
					ret = "m";
					break;
				case HOURS:
					ret = "h";
					break;
				case DAYS:
					ret = "d";
					break;
			}
		}

		return ret;
	}

	private double getValueAsDouble(MetricValue value)
	{
		double ret = 0.0;

		if (TYPE_LONG.equals(value.getType()))
		{
			ret = ((LongValue)value).getValue();
		}
		else if (TYPE_DOUBLE.equals(value.getType()))
		{
			ret = ((DoubleValue)value).getValue();
		}
		else
		{
			logger.debug("Unable to convert MetricValue of type {} to double", value.getType());
		}

		return ret;
	}

	private SingleSample getSinglePointData(List<FormattedMetric.Sample> samples, Attributes attributes, boolean gauge)
	{
		//todo check for no samples
		FormattedMetric.Sample sample = samples.get(0);
		Long nanos = sample.getTime().toEpochMilli() * 1_000_000L; //Because they want nanons
		MetricValue value = sample.getValue();
		String valueType = value.getType();

		PointData pointData = null;
		MetricDataType type = null;

		if (TYPE_LONG.equals(valueType))
		{
			pointData = ImmutableLongPointData.create(0L, nanos, attributes, ((LongValue) value).getValue());
			if (gauge)
				type = MetricDataType.LONG_GAUGE;
			else
				type = MetricDataType.LONG_SUM;
		}
		else if (TYPE_DOUBLE.equals(valueType))
		{
			pointData = ImmutableDoublePointData.create(0L, nanos, attributes, ((DoubleValue) value).getValue());
			if (gauge)
				type = MetricDataType.DOUBLE_GAUGE;
			else
				type = MetricDataType.DOUBLE_SUM;
		}

		return new SingleSample(sample.getMetricName(), type, pointData);
	}

	private MetricData createData(FormattedMetric metric)
	{
		Map<String, String> reportContext = metric.getContext();
		String metricType = reportContext.getOrDefault(TYPE_KEY, "none");

		Map<String, String> metricProperties = metric.getProps();

		//For collectors that know the unit such as ms - this can be set and
		//passed on as a context property
		String unit = getChronoUnit(reportContext.get(CHRONO_UNIT_KEY), "1");

		unit = metricProperties.getOrDefault(OTEL_UNIT, unit);
		boolean isMonotonic = !metricProperties.getOrDefault(OTEL_IS_MONOTONIC, "false").equals("false");

		InstrumentationScopeInfo.create(name);

		AttributesBuilder attributesBuilder = Attributes.builder();
		for (Map.Entry<String, String> tagEntry : metric.getTags().entrySet())
		{
			attributesBuilder.put(tagEntry.getKey(), tagEntry.getValue());
		}
		Attributes attributes = attributesBuilder.build();

		MetricData ret = null;

		if (TYPE_SUMMARY_VALUE.equals(metricType))
		{
			List<ValueAtQuantile> percentileValues = new ArrayList<>();
			double sum = 0.0;
			long count = 0L;

			for (ReportedMetric.Sample sample : metric.getMetric().getSamples())
			{
				SummaryContext sampleContext = (SummaryContext) sample.getSampleContext();
				switch (sampleContext.getSummaryType())
				{
					case SUM:
						sum = getValueAsDouble(sample.getValue());
						break;
					case COUNT:
						count = ((LongValue)sample.getValue()).getValue();
						break;
					case QUANTILE:
						percentileValues.add(ImmutableValueAtQuantile.create(sampleContext.getQuantileValue(), getValueAsDouble(sample.getValue())));
						break;
				}
			}

			FormattedMetric.Sample firstSample = metric.getSamples().get(0);
			Long nanos = firstSample.getTime().toEpochMilli() * 1_000_000L; //Because they want nanons

			ret = ImmutableMetricData.createDoubleSummary(
					Resource.getDefault(), m_scopeInfo,
					firstSample.getMetricName(),
					metric.getHelp(),
					unit,
					ImmutableSummaryData.create(
							Collections.singleton(ImmutableSummaryPointData.create(
									0L,
									nanos,
									attributes,
									count,
									sum,
									percentileValues
							))
					)
			);

		}
		else if (TYPE_GAUGE_VALUE.equals(metricType))
		{
			SingleSample singlePointData = getSinglePointData(metric.getSamples(), attributes, true);
			ret = M4jFactory.metricDataCreate(
					Resource.getDefault(),
					m_scopeInfo,
					singlePointData.getName(),
					metric.getHelp(),
					unit,
					singlePointData.getType(),
					ImmutableGaugeData.create(Collections.singleton(singlePointData.getPointData())));


		}
		else if (TYPE_COUNTER_VALUE.equals(metricType))
		{
			String aggregation = reportContext.getOrDefault(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
			AggregationTemporality temporality;
			if (AGGREGATION_DELTA_VALUE.equals(aggregation))
				temporality = AggregationTemporality.DELTA;
			else
				temporality = AggregationTemporality.CUMULATIVE;

			SingleSample singlePointData = getSinglePointData(metric.getSamples(), attributes, false);
			ret = M4jFactory.metricDataCreate(
					Resource.getDefault(),
					m_scopeInfo,
					singlePointData.getName(),
					metric.getHelp(),
					unit,
					singlePointData.getType(),
					ImmutableSumData.create(isMonotonic, temporality, Collections.singleton(singlePointData.getPointData())));

		}

	return ret;
	}

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		List<MetricData> metricsToSend = new ArrayList<>();

		for (FormattedMetric metric : metrics)
		{
			MetricData data = createData(metric);
			if (data != null)
				metricsToSend.add(data);
		}

		m_exporter.export(metricsToSend);
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}

	//Used for testing
	public void setExporter(MetricExporter exporter)
	{
		m_scopeInfo = InstrumentationScopeInfo.create(name);
		m_exporter = exporter;
	}

	@Override
	public void init(MetricsContext context)
	{
		m_scopeInfo = InstrumentationScopeInfo.create(name);

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
