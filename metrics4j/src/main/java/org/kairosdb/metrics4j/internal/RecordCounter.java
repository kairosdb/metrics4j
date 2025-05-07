package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.helpers.AllCollectors;
import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.Callable;

public class RecordCounter implements AllCollectors
{
	@Override
	public void put(double value)
	{

	}

	@Override
	public void put(Instant time, double value)
	{

	}

	@Override
	public void put(Duration duration)
	{

	}

	@Override
	public void put(Instant time, Duration duration)
	{

	}

	@Override
	public <T> T timeEx(Callable<T> callable) throws Exception
	{
		return null;
	}

	@Override
	public <T> T time(TimeCallable<T> callable)
	{
		return null;
	}

	@Override
	public BlockTimer time()
	{
		return null;
	}

	@Override
	public void put(long value)
	{

	}

	@Override
	public void put(Instant time, long value)
	{

	}

	@Override
	public void put(String value)
	{

	}

	@Override
	public void put(Instant time, String value)
	{

	}

	@Override
	public void put(Instant value)
	{

	}

	@Override
	public void put(Instant time, Instant value)
	{

	}

	@Override
	public Collector clone()
	{
		return null;
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{

	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
