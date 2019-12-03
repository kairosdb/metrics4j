package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.StringValue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StringReporterTest
{
	@Test
	public void testClone()
	{
		StringReporter timer = new StringReporter();
		assertThat(timer.clone()).isNotNull();
	}

	@Test
	public void testOne()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		StringReporter stringReporter = new StringReporter();

		stringReporter.put("hello");
		stringReporter.put("world");

		stringReporter.reportMetric(reporter);

		verify(reporter).put(eq("value"), eq(new StringValue("hello")), any());
		verify(reporter).put(eq("value"), eq(new StringValue("world")), any());
	}
}