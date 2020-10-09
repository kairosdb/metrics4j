package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.TimeDelta;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.util.Clock;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TimeDeltaTest
{
	@Test
	public void testOne()
	{
		Clock clock = mock(Clock.class);
		TimeDelta timeDelta = new TimeDelta(clock);

		when(clock.now()).thenReturn(Instant.parse("2007-12-03T10:16:00.00Z").toEpochMilli());

		timeDelta.put(Instant.parse("2007-12-03T10:17:00.00Z"));

		MetricReporter reporter = mock(MetricReporter.class);

		timeDelta.reportMetric(reporter);

		verify(reporter).put(eq("total"), eq(new LongValue(60_000)));
	}

	@Test  //now flip the timestamps
	public void testTwo()
	{
		Clock clock = mock(Clock.class);
		TimeDelta timeDelta = new TimeDelta(clock);

		when(clock.now()).thenReturn(Instant.parse("2007-12-03T10:17:00.00Z").toEpochMilli());

		timeDelta.put(Instant.parse("2007-12-03T10:16:00.00Z"));

		MetricReporter reporter = mock(MetricReporter.class);

		timeDelta.reportMetric(reporter);

		verify(reporter).put(eq("total"), eq(new LongValue(60_000)));
	}
}
