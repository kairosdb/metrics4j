package org.kairosdb.metrics4j.internal.testing;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.helpers.AllCollectors;
import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class CollectorRecorder implements AllCollectors
{
	public static final String PUT = "put";

	private List<CollectorCall> m_callList = new ArrayList<>();

	public List<CollectorCall> getCallList()
	{
		return m_callList;
	}

	@Override
	public void put(double value)
	{
		m_callList.add(new CollectorCall(PUT, value));
	}

	@Override
	public void put(Instant time, double value)
	{
		m_callList.add(new CollectorCall(PUT, time, value));
	}

	@Override
	public void put(Duration duration)
	{
		m_callList.add(new CollectorCall(PUT, duration));
	}

	@Override
	public void put(Instant time, Duration duration)
	{
		m_callList.add(new CollectorCall(PUT, time, duration));
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
		m_callList.add(new CollectorCall(PUT, value));
	}

	@Override
	public void put(Instant time, long value)
	{
		m_callList.add(new CollectorCall(PUT, time, value));
	}

	@Override
	public void put(String value)
	{
		m_callList.add(new CollectorCall(PUT, value));
	}

	@Override
	public void put(Instant time, String value)
	{
		m_callList.add(new CollectorCall(PUT, time, value));
	}

	@Override
	public void put(Instant value)
	{
		m_callList.add(new CollectorCall(PUT, value));
	}

	@Override
	public void put(Instant time, Instant value)
	{
		m_callList.add(new CollectorCall(PUT, time, value));
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
