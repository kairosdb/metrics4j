package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.MINUTES, false);

		timer.put(Duration.ofHours(5));
		timer.put(Duration.ofMinutes(1));
		timer.put(Duration.ofMinutes(60));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(1));
		verify(reporter).put("max", new LongValue(300));
		verify(reporter).put("total", new LongValue(361));
		verify(reporter).put("count", new LongValue(3));
		verify(reporter).put("avg", new DoubleValue(361.0/3.0));
	}

	@Test
	public void testTwo()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		SimpleTimerMetric timer = new SimpleTimerMetric(ChronoUnit.SECONDS, false);

		timer.put(Duration.ofMillis(399));
		timer.put(Duration.ofMillis(1));
		timer.put(Duration.ofMillis(600));

		timer.reportMetric(reporter);

		verify(reporter).put("min", new LongValue(0));
		verify(reporter).put("max", new LongValue(0));
		verify(reporter).put("total", new LongValue(1));
		verify(reporter).put("count", new LongValue(3));
		verify(reporter).put("avg", new DoubleValue(1.0/3.0));
	}
}