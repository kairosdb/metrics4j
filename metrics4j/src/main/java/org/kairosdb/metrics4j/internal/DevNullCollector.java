package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.collectors.TimeCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;

public class DevNullCollector extends TimerCollector implements LongCollector,
		DoubleCollector, DurationCollector, MetricCollector, TimeCollector, StringCollector
{
	@Override
	public void put(double value)
	{
	}

	@Override
	public void put(Duration duration)
	{
	}

	@Override
	public void put(long value)
	{
	}

	@Override
	public void reportMetric(MetricReporter reportedMetric)
	{

	}

	@Override
	public void init(MetricsContext context)
	{
	}

	@Override
	public Collector clone()
	{
		return this;
	}

	@Override
	public void put(Instant time)
	{
	}

	@Override
	public void put(String value)
	{
	}
}
