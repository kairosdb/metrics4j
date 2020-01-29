package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.DoubleGauge;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DoubleGaugeTest
{
	@Test
	public void test_gauge()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		DoubleGauge gauge = new DoubleGauge();

		gauge.put(1.1);
		gauge.put(2.2);
		gauge.put(3.3);

		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new DoubleValue(3.3));

		gauge.put(1.1);
		gauge.put(3.3);
		gauge.put(2.2);


		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new DoubleValue(2.2));
	}

	@Test
	public void test_gaugeWithReset()
	{
		MetricReporter reporter = mock(MetricReporter.class);
		DoubleGauge gauge = new DoubleGauge(true);

		gauge.put(1.1);
		gauge.put(2.2);
		gauge.put(3.3);

		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new DoubleValue(3.3));

		gauge.reportMetric(reporter);
		verify(reporter).put("gauge", new DoubleValue(0.0));

		gauge.put(1.1);
		gauge.put(3.3);
		gauge.put(2.2);


		gauge.reportMetric(reporter);

		verify(reporter).put("gauge", new DoubleValue(2.2));
	}
}