package org.kairosdb.metrics4j.collectors;

import lombok.Getter;
import lombok.Setter;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.util.concurrent.atomic.AtomicLong;

public class LongGauge implements LongCollector
{
	private final AtomicLong m_gauge = new AtomicLong(0);

	@Setter
	private boolean reset = false;

	public LongGauge(boolean reset)
	{
		super();
		this.reset = reset;
	}

	public LongGauge()
	{
		this(false);
	}


	@Override
	public void put(long value)
	{
		m_gauge.set(value);
	}

	@Override
	public Collector clone()
	{
		return new LongGauge(reset);
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;

		if (reset)
			value = m_gauge.getAndSet(0);
		else
			value = m_gauge.get();

		metricReporter.put("gauge", new LongValue(value));
	}

}
