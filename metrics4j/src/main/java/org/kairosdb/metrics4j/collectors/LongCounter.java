package org.kairosdb.metrics4j.collectors;

import lombok.Getter;
import lombok.Setter;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.util.concurrent.atomic.AtomicLong;


public class LongCounter implements LongCollector
{
	private final AtomicLong m_count = new AtomicLong(0);

	@Setter
	private boolean reset = false;

	public LongCounter(boolean reset)
	{
		super();
		this.reset = reset;
	}

	public LongCounter()
	{
		this(false);
	}

	public void put(long count)
	{
		m_count.addAndGet(count);
	}

	public Collector reset()
	{
		m_count.set(0);
		return this;
	}


	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;

		if (reset)
			value = m_count.getAndSet(0);
		else
			value = m_count.longValue();

		metricReporter.put("count", new LongValue(value));
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public Collector clone()
	{
		return new LongCounter(reset);
	}
}
