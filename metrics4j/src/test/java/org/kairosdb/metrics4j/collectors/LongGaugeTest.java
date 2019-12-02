package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LongGaugeTest
{
	@Test
	public void testGauge()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		LongGauge gauge = new LongGauge();


		gauge.put(1);
		gauge.put(3);
		gauge.put(2);

		gauge.reportMetric(reporter);

		gauge.reportMetric(reporter);
		verify(reporter, times(2)).put("gauge", new LongValue(2));

		gauge.put(1);
		gauge.put(2);
		gauge.put(3);

		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new LongValue(3));
	}

	@Test
	public void testResetGauge()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		LongGauge gauge = new LongGauge(true);


		gauge.put(1);
		gauge.put(3);
		gauge.put(2);

		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new LongValue(2));

		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new LongValue(0));

		gauge.put(1);
		gauge.put(2);
		gauge.put(3);

		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new LongValue(3));
	}
}