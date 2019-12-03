package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

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
		SimpleStats stats = new SimpleStats(false);

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

		verifyNoMoreInteractions(reporter);
	}

	@Test
	public void testReportZero()
	{
		MetricReporter reporter = mock(MetricReporter.class);
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