package org.kairosdb.metrics4j.collectors;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.internal.ReportingContext;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class LongCounterTest implements ReportingContext
{
	@Test
	public void test_countingValues()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		LongCounter counter = new LongCounter();

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);
		verify(reporter).put("count", new LongValue(6));

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);
		verify(reporter).put("count", new LongValue(12));
	}

	@Test
	public void test_countingWithReset()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		LongCounter counter = new LongCounter(true, true);

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);

		verify(reporter, times(2)).put("count", new LongValue(6));

		counter.put(0);

		counter.reportMetric(reporter);

		verify(reporter, times(1)).put("count", new LongValue(0));
	}

	@Test
	public void test_countingNotReportingZero()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		LongCounter counter = new LongCounter(true, false);
		counter.init(null);

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);

		counter.put(0);

		counter.reportMetric(reporter);

		Map<String, String> context = new HashMap<>();
		context.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
		context.put(TYPE_KEY, TYPE_COUNTER_VALUE);

		verify(reporter, times(2)).setContext(context);
		verify(reporter, times(2)).put("count", new LongValue(6));

		verifyNoMoreInteractions(reporter);

	}

	@Test
	public void test_context()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		LongCounter counter = new LongCounter(true, false);
		counter.init(null);

		counter.put(1);

		counter.reportMetric(reporter);

		Map<String, String> context = Maps.newHashMap(TYPE_KEY, TYPE_COUNTER_VALUE);
		context.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
		verify(reporter, times(1)).setContext(context);
		verify(reporter, times(1)).put("count", new LongValue(1));

		verifyNoMoreInteractions(reporter);
	}
}