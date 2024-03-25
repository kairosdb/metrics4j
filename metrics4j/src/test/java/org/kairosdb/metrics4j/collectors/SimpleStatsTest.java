package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.SimpleStats;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_SUMMARY_VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class SimpleStatsTest
{
	@Test
	public void testClone()
	{
		SimpleStats stats = new SimpleStats(true);
		assertThat(stats).isEqualTo(stats.clone());
	}

	@Test
	public void testSimpleStats()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample mockSample = mock(ReportedMetricImpl.SampleImpl.class);
		when(reporter.put(anyString(), any())).thenReturn(mockSample);
		SimpleStats stats = new SimpleStats(false);
		stats.init(null);

		stats.put(1);
		stats.put(2);
		stats.put(3);

		stats.reportMetric(reporter);
		verify(reporter).put("min", new LongValue(1));
		verify(reporter).put("max", new LongValue(3));
		verify(reporter).put("sum", new LongValue(6));
		verify(reporter).put("count", new LongValue(3));
		verify(reporter).put("avg", new DoubleValue(2.0));

		HashMap<String, String> context = new HashMap<>();
		context.put(TYPE_KEY, TYPE_SUMMARY_VALUE);
		verify(reporter, times(1)).setContext(context);

		stats.reportMetric(reporter);

		verifyNoMoreInteractions(reporter);
	}

	@Test
	public void testReportZero()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample mockSample = mock(ReportedMetricImpl.SampleImpl.class);
		when(reporter.put(anyString(), any())).thenReturn(mockSample);
		SimpleStats stats = new SimpleStats(true);

		stats.put(1);
		stats.put(2);
		stats.put(3);

		stats.reportMetric(reporter);
		verify(reporter).put("min", new LongValue(1));
		verify(reporter).put("max", new LongValue(3));
		verify(reporter).put("sum", new LongValue(6));
		verify(reporter).put("count", new LongValue(3));
		verify(reporter).put("avg", new DoubleValue(2.0));

		stats.reportMetric(reporter);
		verify(reporter).put("min", new LongValue(0));
		verify(reporter).put("max", new LongValue(0));
		verify(reporter).put("sum", new LongValue(0));
		verify(reporter).put("count", new LongValue(0));
		verify(reporter).put("avg", new DoubleValue(0.0));
	}
}