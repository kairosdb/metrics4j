package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.util.Map;
import java.util.function.DoubleSupplier;

public class DoubleLambdaCollectorAdaptor implements MetricCollector
{
	private final DoubleSupplier m_lambda;

	public DoubleLambdaCollectorAdaptor(DoubleSupplier lambda)
	{
		m_lambda = lambda;
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		metricReporter.put("value", new DoubleValue(m_lambda.getAsDouble()));
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
