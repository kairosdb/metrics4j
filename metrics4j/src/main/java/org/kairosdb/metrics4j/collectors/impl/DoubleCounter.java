package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Instant;

@ToString
@EqualsAndHashCode
public class DoubleCounter implements DoubleCollector
{
	protected double m_count;

	@EqualsAndHashCode.Exclude
	protected Object m_counterLock = new Object();

	@Setter
	protected boolean reset;

	@Setter
	protected boolean reportZero;

	public DoubleCounter(boolean reset, boolean reportZero)
	{
		this.reset = reset;
		this.reportZero = reportZero;
	}

	public DoubleCounter()
	{
		this(false, true);
	}

	@Override
	public void put(double value)
	{
		synchronized (m_counterLock)
		{
			m_count += value;
		}
	}

	@Override
	public void put(Instant time, double value)
	{
		put(value);
	}

	@Override
	public Collector clone()
	{
		return new DoubleCounter(reset, reportZero);
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		synchronized (m_counterLock)
		{
			if (m_count != 0.0 || reportZero)
				metricReporter.put("count", new DoubleValue(m_count));

			if (reset)
				m_count = 0.0;
		}
	}
}
