package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.collectors.TimeCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 Counts the number of times put is called to pass on a metric.
 */
public class PutCounter extends TimerCollector implements LongCollector, DoubleCollector,
		StringCollector, DurationCollector, TimeCollector, MetricCollector
{
	@EqualsAndHashCode.Exclude
	protected final AtomicLong m_count = new AtomicLong(0);

	@Setter
	protected boolean reset = false;

	@Setter
	protected boolean reportZero = true;

	public PutCounter(boolean reset, boolean reportZero)
	{
		super();
		this.reset = reset;
		this.reportZero = reportZero;
	}

	public PutCounter()
	{
		this(false, true);
	}

	public void put(long count)
	{
		m_count.incrementAndGet();
	}


	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;

		if (reset)
			value = m_count.getAndSet(0);
		else
			value = m_count.longValue();

		if (value != 0L || reportZero)
			metricReporter.put("count", new LongValue(value));
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public Collector clone()
	{
		return new LongCounter(reset, reportZero);
	}

	@Override
	public void put(double value)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Duration duration)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(String value)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Instant time)
	{
		m_count.incrementAndGet();
	}
}
