package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LongCounterTest
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
		LongCounter counter = new LongCounter(true);

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);

		counter.put(1);
		counter.put(2);
		counter.put(3);

		counter.reportMetric(reporter);

		verify(reporter, times(2)).put("count", new LongValue(6));
	}
}