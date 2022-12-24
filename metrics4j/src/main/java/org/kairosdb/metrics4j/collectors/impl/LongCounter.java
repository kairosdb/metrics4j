package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


@ToString
@EqualsAndHashCode
public class LongCounter implements LongCollector
{
	@EqualsAndHashCode.Exclude
	protected final AtomicLong m_count = new AtomicLong(0);

	@Setter
	protected boolean reset = false;

	@Setter
	protected boolean reportZero = true;

	public LongCounter(boolean reset, boolean reportZero)
	{
		super();
		this.reset = reset;
		this.reportZero = reportZero;
	}

	public LongCounter()
	{
		this(false, true);
	}

	@Override
	public void put(long count)
	{
		m_count.addAndGet(count);
	}

	@Override
	public void put(Instant time, long count)
	{
		put(count);
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
	public void setContextProperties(Map<String, String> contextProperties)
	{

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
}
