package org.kairosdb.metrics4jplugin.opentelemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.collectors.impl.BagCollector;
import org.kairosdb.metrics4j.collectors.impl.DoubleCounter;
import org.kairosdb.metrics4j.collectors.impl.DoubleGauge;
import org.kairosdb.metrics4j.collectors.impl.LastTime;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.collectors.impl.LongGauge;
import org.kairosdb.metrics4j.collectors.impl.MaxLongGauge;
import org.kairosdb.metrics4j.collectors.impl.PutCounter;
import org.kairosdb.metrics4j.collectors.impl.SimpleStats;
import org.kairosdb.metrics4j.collectors.impl.SimpleTimerMetric;
import org.kairosdb.metrics4j.collectors.impl.StringReporter;
import org.kairosdb.metrics4j.collectors.impl.TimestampCounter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.internal.CollectorContextImpl;
import org.kairosdb.metrics4j.internal.SinkQueue;
import org.kairosdb.metrics4j.internal.StaticCollectorCollection;
import org.kairosdb.metrics4j.internal.TagKey;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OtelSinkTest
{
	private StaticCollectorCollection m_collectorCollection;
	private CollectorContextImpl m_collectorContext;
	@Mock
	private MetricExporter m_mockExporter;
	private Instant m_now;
	private final TagKey m_tagKey = TagKey.newBuilder().addTag("host", "home").build();

	@Captor
	private ArgumentCaptor<Collection<MetricData>> m_metricCaptor;


	@BeforeEach
	public void beforeTest()
	{
		MockitoAnnotations.initMocks(this);
		m_now = Instant.now();

		OtelSink sink = new OtelSink();
		sink.setName("otel");
		sink.setExporter(m_mockExporter);

		ArgKey argKey = new SimpleArgKey(Collections.emptyList(), "testOtelSink", this.getClass().getName());
		m_collectorCollection = new StaticCollectorCollection(argKey, Collections.emptyMap());
		m_collectorContext = new CollectorContextImpl(m_collectorCollection, argKey);
		m_collectorContext.addSinkQueue(Collections.singletonList(new SinkQueue(sink, "otel")));
		Map<String, Formatter> formatters = new HashMap<>();
		formatters.put("otel", sink.getDefaultFormatter());
		m_collectorContext.setFormatters(formatters);
	}

	private <T extends Collector> T initCollector(T collector)
	{
		collector.init(mock(MetricsContext.class));
		m_collectorCollection.addCollector(m_tagKey, collector);

		return collector;
	}

	private void reportMetrics()
	{
		m_collectorContext.reportMetrics(m_now);
		m_collectorContext.getSinkQueueList().forEach(SinkQueue::flush);
	}

	private void verifyMetricData(MetricData metricData, String unit, MetricDataType type, String metricSuffix, String... contains)
	{
		verifyMetricData(metricData, unit, type, metricSuffix, m_now, contains);
	}

	private void verifyMetricData(MetricData metricData, String unit, MetricDataType type, String metricSuffix, Instant time, String... contains)
	{
		assertThat(metricData.getUnit()).isEqualTo(unit);
		assertThat(metricData.getType()).isEqualTo(type);
		if (metricSuffix != null)
			assertThat(metricData.getName()).isEqualTo("org.kairosdb.metrics4jplugin.opentelemetry.OtelSinkTest.testOtelSink."+metricSuffix);
		else
			assertThat(metricData.getName()).isEqualTo("org.kairosdb.metrics4jplugin.opentelemetry.OtelSinkTest.testOtelSink");
		Data<?> data = metricData.getData();
		//System.out.println(data.toString());

		PointData pointData = data.getPoints().iterator().next();
		if (time != null)
			assertThat(pointData.getEpochNanos()).isEqualTo(time.toEpochMilli() * 1_000_000L);

		assertThat(pointData.getAttributes().asMap()).containsKey(AttributeKey.stringKey("host"));

		String dataString = data.toString();
		for (String check : contains)
		{
			assertThat(dataString).containsSequence(check);
		}

	}


	@Test
	void testBagCollector()
	{
		BagCollector collector = initCollector(new BagCollector());

		collector.put(m_now, 42.2);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.DOUBLE_SUM,
				"value",
				"monotonic=false",
				"aggregationTemporality=DELTA");

	}

	@Test
	void testDoubleCounter()
	{
		DoubleCounter collector = new DoubleCounter();
		collector.setReset(false);
		initCollector(collector);

		collector.put(42.2);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.DOUBLE_SUM,
				"count",
				"monotonic=false",
				"aggregationTemporality=CUMULATIVE");
	}

	@Test
	void testDoubleCounter_rest()
	{
		DoubleCounter collector = new DoubleCounter();
		collector.setReset(true);
		initCollector(collector);

		collector.put(42.2);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.DOUBLE_SUM,
				"count",
				"monotonic=false",
				"aggregationTemporality=DELTA");
	}

	@Test
	void testDoubleGauge()
	{
		DoubleGauge collector = new DoubleGauge();
		collector.setReset(true);
		initCollector(collector);

		collector.put(42.2);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.DOUBLE_GAUGE,
				"gauge");
	}

	@Test
	void testLastTime_nanos()
	{
		LastTime collector = new LastTime();
		collector.setReportUnit(ChronoUnit.NANOS);
		initCollector(collector);

		collector.put(Duration.ofSeconds(42));

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"ns",
				MetricDataType.LONG_GAUGE,
				"value");
	}

	@Test
	void testLastTime_micros()
	{
		LastTime collector = new LastTime();
		collector.setReportUnit(ChronoUnit.MICROS);
		initCollector(collector);

		collector.put(Duration.ofSeconds(42));

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"us",
				MetricDataType.LONG_GAUGE,
				"value");
	}

	@Test
	void testLastTime_millis()
	{
		LastTime collector = new LastTime();
		collector.setReportUnit(ChronoUnit.MILLIS);
		initCollector(collector);

		collector.put(Duration.ofSeconds(42));

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"ms",
				MetricDataType.LONG_GAUGE,
				"value");
	}

	@Test
	void testLastTime_seconds()
	{
		LastTime collector = new LastTime();
		collector.setReportUnit(ChronoUnit.SECONDS);
		initCollector(collector);

		collector.put(Duration.ofSeconds(42));

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"s",
				MetricDataType.LONG_GAUGE,
				"value");
	}

	@Test
	void testLastTime_minutes()
	{
		LastTime collector = new LastTime();
		collector.setReportUnit(ChronoUnit.MINUTES);
		collector.setReportFormat(TimerCollector.ReportFormat.DOUBLE);
		initCollector(collector);

		collector.put(Duration.ofSeconds(42));

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"m",
				MetricDataType.DOUBLE_GAUGE,
				"value");
	}

	@Test
	void testLastTime_hours()
	{
		LastTime collector = new LastTime();
		collector.setReportUnit(ChronoUnit.HOURS);
		collector.setReportFormat(TimerCollector.ReportFormat.DOUBLE);
		initCollector(collector);

		collector.put(Duration.ofSeconds(42));

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"h",
				MetricDataType.DOUBLE_GAUGE,
				"value");
	}

	@Test
	void testLastTime_days()
	{
		LastTime collector = new LastTime();
		collector.setReportUnit(ChronoUnit.DAYS);
		collector.setReportFormat(TimerCollector.ReportFormat.DOUBLE);
		initCollector(collector);

		collector.put(Duration.ofSeconds(42));

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"d",
				MetricDataType.DOUBLE_GAUGE,
				"value");
	}

	@Test
	void testLongCounter()
	{
		LongCounter collector = new LongCounter();
		collector.setReset(false);
		initCollector(collector);

		collector.put(42);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.LONG_SUM,
				"count",
				"monotonic=false",
				"aggregationTemporality=CUMULATIVE");
	}

	@Test
	void testLongCounter_reset()
	{
		LongCounter collector = new LongCounter();
		collector.setReset(true);
		initCollector(collector);

		collector.put(42);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.LONG_SUM,
				"count",
				"monotonic=false",
				"aggregationTemporality=DELTA");
	}

	@Test
	void testLongGauge()
	{
		LongGauge collector = new LongGauge();
		collector.setReset(true);
		initCollector(collector);

		collector.put(42);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.LONG_GAUGE,
				"gauge");
	}

	@Test
	void testMaxLongGauge()
	{
		MaxLongGauge collector = new MaxLongGauge();
		collector.setReset(true);
		initCollector(collector);

		collector.put(42);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.LONG_GAUGE,
				"gauge");
	}

	@Test
	void testPutCounter()
	{
		PutCounter collector = new PutCounter();
		collector.setReset(false);
		initCollector(collector);

		collector.put(42);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		verifyMetricData(m_metricCaptor.getValue().iterator().next(),
				"1",
				MetricDataType.LONG_SUM,
				"count",
				"monotonic=false",
				"aggregationTemporality=CUMULATIVE",
				"value=1");
	}

	@Test
	void testSimpleStats()
	{
		SimpleStats collector = new SimpleStats();
		initCollector(collector);

		collector.put(1);
		collector.put(2);
		collector.put(3);

		reportMetrics();

		verify(m_mockExporter).export(m_metricCaptor.capture());

		Collection<MetricData> value = m_metricCaptor.getValue();

		verifyMetricData(value.iterator().next(),
				"1",
				MetricDataType.SUMMARY,
				null,
				"count=3",
				"sum=6.0",
				"quantile=0.0, value=1.0",
				"quantile=1.0, value=3.0",
				"quantile=0.5, value=2.0");
	}

	@Test
	void testSimpleTimerMetric()
	{
		SimpleTimerMetric collector = new SimpleTimerMetric();
		initCollector(collector);

		collector.put(Duration.ofMillis(100));
		collector.put(Duration.ofMillis(200));
		collector.put(Duration.ofMillis(300));

		reportMetrics();

		verify(m_mockExporter).export(m_metricCaptor.capture());

		Collection<MetricData> value = m_metricCaptor.getValue();

		verifyMetricData(value.iterator().next(),
				"ms",
				MetricDataType.SUMMARY,
				null,
				"count=3",
				"sum=600.0",
				"quantile=0.0, value=100.0",
				"quantile=1.0, value=300.0",
				"quantile=0.5, value=200.0");
	}

	@Test
	void testStringReporter()
	{
		StringReporter collector = new StringReporter();
		initCollector(collector);

		collector.put("42");

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		//String values are not sent via otel collector
		assertThat(m_metricCaptor.getValue().iterator().hasNext()).isFalse();
	}

	@Test
	void testTimestampCounter()
	{
		TimestampCounter collector = new TimestampCounter();
		initCollector(collector);

		collector.put(m_now);

		reportMetrics();

		//Verify output
		verify(m_mockExporter).export(m_metricCaptor.capture());

		MetricData metricData = m_metricCaptor.getValue().iterator().next();
		verifyMetricData(metricData,
				"1",
				MetricDataType.LONG_SUM,
				"count",
				(Instant)null,
				"monotonic=false",
				"aggregationTemporality=DELTA",
				"value=1");
	}
}
