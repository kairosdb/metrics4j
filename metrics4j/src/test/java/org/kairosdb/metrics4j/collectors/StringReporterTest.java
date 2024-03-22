package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.StringReporter;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.reporting.StringValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
		ReportedMetric.Sample mockSample = mock(ReportedMetricImpl.SampleImpl.class);
		when(reporter.put(anyString(), any())).thenReturn(mockSample);
		StringReporter stringReporter = new StringReporter();

		stringReporter.put("hello");
		stringReporter.put("world");

		stringReporter.reportMetric(reporter);

		verify(reporter).put(eq("value"), eq(new StringValue("hello")));
		verify(reporter).put(eq("value"), eq(new StringValue("world")));
	}
}