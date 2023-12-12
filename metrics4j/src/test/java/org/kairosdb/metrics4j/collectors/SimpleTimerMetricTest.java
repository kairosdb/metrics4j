package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.SimpleTimerMetric;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SimpleTimerMetricTest
{
	@Test
	public void testClone()
	{
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.DAYS, true);
		assertThat(timer).isEqualTo(timer.clone());
	}

	@Test
	public void testOne()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.MINUTES, false);
		timer.init(null);

		timer.put(Duration.ofHours(5));
		timer.put(Duration.ofMinutes(1));
		timer.put(Duration.ofMinutes(60));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(1));
		verify(reporter).put("max", new LongValue(300));
		verify(reporter).put("total", new LongValue(361));
		verify(reporter).put("count", new LongValue(3));
		verify(reporter).put("avg", new DoubleValue(120.333));
	}

	@Test
	public void testTwo()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.SECONDS, false);
		timer.init(null);

		timer.put(Duration.ofMillis(399));
		timer.put(Duration.ofMillis(1));
		timer.put(Duration.ofMillis(600));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(0));
		verify(reporter).put("max", new LongValue(0));
		verify(reporter).put("total", new LongValue(1));
		verify(reporter).put("count", new LongValue(3));
		verify(reporter).put("avg", new DoubleValue(0.333));
	}

	@Test
	public void testReportNothing()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.SECONDS, false);
		timer.init(null);

		timer.put(Duration.ofSeconds(42));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(42));
		verify(reporter).put("max", new LongValue(42));
		verify(reporter).put("total", new LongValue(42));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(42));

		verify(reporter).setContext(anyMap());

		timer.reportMetric(reporter);

		verifyNoMoreInteractions(reporter);
	}

	@Test
	public void testReportZeros()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.SECONDS, true);
		timer.init(null);

		timer.put(Duration.ofSeconds(42));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(42));
		verify(reporter).put("max", new LongValue(42));
		verify(reporter).put("total", new LongValue(42));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(42));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(0));
		verify(reporter).put("max", new LongValue(0));
		verify(reporter).put("total", new LongValue(0));
		verify(reporter).put("count", new LongValue(0));
		verify(reporter).put("avg", new DoubleValue(0));
	}

	@Test
	public void testNanos()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.NANOS, false);
		timer.init(null);

		timer.put(Duration.ofMinutes(1));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(60000000000L));
		verify(reporter).put("max", new LongValue(60000000000L));
		verify(reporter).put("total", new LongValue(60000000000L));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(60000000000L));
	}

	@Test
	public void testMicros()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.MICROS, false);
		timer.init(null);

		timer.put(Duration.ofMinutes(1));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(60000000L));
		verify(reporter).put("max", new LongValue(60000000L));
		verify(reporter).put("total", new LongValue(60000000L));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(60000000L));
	}

	@Test
	public void testMillis()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.MILLIS, false);
		timer.init(null);

		timer.put(Duration.ofMinutes(1));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(60000L));
		verify(reporter).put("max", new LongValue(60000L));
		verify(reporter).put("total", new LongValue(60000L));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(60000L));
	}

	@Test
	public void testSeconds()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.SECONDS, false);
		timer.init(null);

		timer.put(Duration.ofMinutes(1));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(60L));
		verify(reporter).put("max", new LongValue(60L));
		verify(reporter).put("total", new LongValue(60L));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(60L));
	}

	@Test
	public void testMinutes()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.MINUTES, false);
		timer.init(null);

		timer.put(Duration.ofHours(1));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(60L));
		verify(reporter).put("max", new LongValue(60L));
		verify(reporter).put("total", new LongValue(60L));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(60L));
	}

	@Test
	public void testHours()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.HOURS, false);
		timer.init(null);

		timer.put(Duration.ofDays(1));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(24L));
		verify(reporter).put("max", new LongValue(24L));
		verify(reporter).put("total", new LongValue(24L));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(24L));
	}

	@Test
	public void testDays()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample sample = mock(ReportedMetric.Sample.class);
		when(reporter.put(anyString(), any())).thenReturn(sample);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.DAYS, false);
		timer.init(null);

		timer.put(Duration.ofHours(48));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(2L));
		verify(reporter).put("max", new LongValue(2L));
		verify(reporter).put("total", new LongValue(2L));
		verify(reporter).put("count", new LongValue(1));
		verify(reporter).put("avg", new DoubleValue(2L));
	}

}