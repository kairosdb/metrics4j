package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.util.function.LongSupplier;

public class LongLambdaCollectorAdaptor implements MetricCollector
{
	private final LongSupplier m_lambda;

	public LongLambdaCollectorAdaptor(LongSupplier lambda)
	{
		m_lambda = lambda;
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value = m_lambda.getAsLong();
		metricReporter.put("value", new LongValue(value));
	}
}
