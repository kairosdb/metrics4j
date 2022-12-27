package org.kairosdb.metrics4j.internal;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.reporting.DoubleValue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DoubleTimeReporterTest
{
	@Test
	public void testNanos()
	{
		DoubleTimeReporter reporter = new DoubleTimeReporter(ChronoUnit.NANOS);

		assertThat(reporter.getValue(Duration.ofNanos(123456789L))).isEqualTo(new DoubleValue(123456789.0));
	}

	@Test
	public void testMicros()
	{
		DoubleTimeReporter reporter = new DoubleTimeReporter(ChronoUnit.MICROS);

		assertThat(reporter.getValue(Duration.ofNanos(123456789L))).isEqualTo(new DoubleValue(123456.789));
	}


	@Test
	public void testMillis()
	{
		DoubleTimeReporter reporter = new DoubleTimeReporter(ChronoUnit.MILLIS);

		assertThat(reporter.getValue(Duration.ofNanos(123456789L))).isEqualTo(new DoubleValue(123.456));
	}

	@Test
	public void testSeconds()
	{
		DoubleTimeReporter reporter = new DoubleTimeReporter(ChronoUnit.SECONDS);

		assertThat(reporter.getValue(Duration.ofNanos(123456789L))).isEqualTo(new DoubleValue(0.123));
	}

	@Test
	public void testMinutes()
	{
		DoubleTimeReporter reporter = new DoubleTimeReporter(ChronoUnit.MINUTES);

		assertThat(reporter.getValue(Duration.ofMillis(123456789L))).isEqualTo(new DoubleValue(2057.613));
	}

	@Test
	public void testHours()
	{
		DoubleTimeReporter reporter = new DoubleTimeReporter(ChronoUnit.HOURS);

		assertThat(reporter.getValue(Duration.ofMillis(123456789L))).isEqualTo(new DoubleValue(34.293));
	}

	@Test
	public void testDays()
	{
		DoubleTimeReporter reporter = new DoubleTimeReporter(ChronoUnit.DAYS);

		assertThat(reporter.getValue(Duration.ofMillis(123456789L))).isEqualTo(new DoubleValue(1.428));
	}
}

