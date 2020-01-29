package org.kairosdb.metrics4j.collectors.impl;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;

/**
 Collector to use if you want to effectively turn off a source.
 */
public class NullCollector extends TimerCollector implements LongCollector, DoubleCollector, StringCollector
{
	@Override
	public void put(double value)
	{
	}

	@Override
	public void put(long value)
	{
	}

	@Override
	public Collector clone()
	{
		return this;
	}

	@Override
	public void init(MetricsContext context)
	{
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		//Nothing to report
	}

	@Override
	public void put(String value)
	{
	}

	@Override
	public void put(Duration duration)
	{
	}
}
