package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.DoubleCounter;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.util.HashMap;
import java.util.Map;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class DoubleCounterTest
{
	@Test
	public void test_countingValues()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		DoubleCounter counter = new DoubleCounter();

		counter.put(1.1);
		counter.put(2.2);
		counter.put(3.3);

		counter.reportMetric(reporter);
		verify(reporter).put("count", new DoubleValue(6.6));

		counter.put(1.1);
		counter.put(2.2);
		counter.put(3.3);

		counter.reportMetric(reporter);
		verify(reporter).put("count", new DoubleValue(13.2));
	}

	@Test
	public void test_countingWithReset()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		DoubleCounter counter = new DoubleCounter(true, true);

		counter.put(1.1);
		counter.put(2.2);
		counter.put(3.3);

		counter.reportMetric(reporter);

		counter.put(1.1);
		counter.put(2.2);
		counter.put(3.3);

		counter.reportMetric(reporter);

		verify(reporter, times(2)).put("count", new DoubleValue(6.6));

		counter.put(0.0);

		counter.reportMetric(reporter);

		verify(reporter, times(1)).put("count", new DoubleValue(0.0));
	}

	@Test
	public void test_countingNotReportingZero()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		DoubleCounter counter = new DoubleCounter(true, false);
		counter.init(null);

		counter.put(1.1);
		counter.put(2.2);
		counter.put(3.3);

		counter.reportMetric(reporter);

		counter.put(1.1);
		counter.put(2.2);
		counter.put(3.3);

		counter.reportMetric(reporter);

		verify(reporter, times(2)).put("count", new DoubleValue(6.6));

		Map<String, String> context = new HashMap<>();
		context.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
		context.put(TYPE_KEY, TYPE_COUNTER_VALUE);
		verify(reporter, times(2)).setContext(context);

		counter.put(0.0);

		counter.reportMetric(reporter);

		verifyNoMoreInteractions(reporter);
	}
}
