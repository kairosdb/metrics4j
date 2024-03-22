package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.TimestampCounter;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.util.Clock;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TimestampCounterTest
{
	@Test
	public void testTimestampIncrementAfter20Sec()
	{
		Clock clock = mock(Clock.class);
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample mockSample = mock(ReportedMetricImpl.SampleImpl.class);
		when(reporter.put(anyString(), any())).thenReturn(mockSample);

		//The reporting will occur 20sec apart
		when(clock.now()).thenReturn(
				Instant.parse("2007-12-03T10:16:00.00Z").toEpochMilli(),
				Instant.parse("2007-12-03T10:16:20.00Z").toEpochMilli(),
				Instant.parse("2007-12-13T10:16:20.00Z").toEpochMilli(),
				Instant.parse("2007-12-16T10:16:20.00Z").toEpochMilli());

		TimestampCounter counter = new TimestampCounter(clock);

		counter.put(Instant.parse("2007-12-03T10:15:30.00Z"));

		counter.reportMetric(reporter);


		counter.put(Instant.parse("2007-12-03T10:15:30.00Z"));

		counter.reportMetric(reporter);


		//Now report the same timestamp 10 days later
		counter.put(Instant.parse("2007-12-03T10:15:30.00Z"));

		counter.reportMetric(reporter);

		//Now 3 days later
		counter.put(Instant.parse("2007-12-03T10:15:30.00Z"));

		counter.reportMetric(reporter);

		Instant reportInstant = Instant.parse("2007-12-03T10:15:13.848Z");
		verify(mockSample, times(1)).setTime(eq(reportInstant));

		//The reporting timestamp should only increment one millisecond
		reportInstant = Instant.parse("2007-12-03T10:15:13.849Z");
		verify(mockSample, times(1)).setTime(eq(reportInstant));

		reportInstant = Instant.parse("2007-12-03T10:15:57.049Z");
		verify(mockSample, times(1)).setTime(eq(reportInstant));

		//timestamp is within the minute and loopping around
		reportInstant = Instant.parse("2007-12-03T10:15:10.009Z");
		verify(mockSample, times(1)).setTime(eq(reportInstant));

		verify(reporter, times(4)).put(eq("count"), eq(new LongValue(1)));
		verify(reporter, times(4)).setContext(anyMap());

		verifyNoMoreInteractions(reporter);
	}

	@Test
	public void testBuckets()
	{
		Clock clock = mock(Clock.class);
		MetricReporter reporter = mock(MetricReporter.class);
		ReportedMetric.Sample mockSample = mock(ReportedMetricImpl.SampleImpl.class);
		when(reporter.put(anyString(), any())).thenReturn(mockSample);

		//using zero so there is no offset from timestamp
		when(clock.now()).thenReturn(0L);

		TimestampCounter counter = new TimestampCounter(clock);

		//3 in one bucket and 2 in the other
		counter.put(Instant.parse("2007-12-03T10:15:30.00Z"));
		counter.put(Instant.parse("2007-12-03T10:15:30.00Z"));
		counter.put(Instant.parse("2007-12-03T10:15:31.00Z"));
		counter.put(Instant.parse("2007-12-03T10:17:30.00Z"));
		counter.put(Instant.parse("2007-12-03T10:17:35.01Z"));

		counter.reportMetric(reporter);

		Instant reportInstant = Instant.parse("2007-12-03T10:15:00.00Z");
		verify(reporter, times(1)).put(eq("count"), eq(new LongValue(3)));
		verify(mockSample, times(1)).setTime(eq(reportInstant));

		reportInstant = Instant.parse("2007-12-03T10:17:00.00Z");
		verify(reporter, times(1)).put(eq("count"), eq(new LongValue(2)));
		verify(mockSample, times(1)).setTime(eq(reportInstant));

		verify(reporter, times(1)).setContext(anyMap());

		verifyNoMoreInteractions(reporter);

	}
}
