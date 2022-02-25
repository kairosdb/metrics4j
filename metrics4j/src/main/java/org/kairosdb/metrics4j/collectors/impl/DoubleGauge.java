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
public class DoubleGauge implements DoubleCollector
{
	protected double m_gauge = 0.0;

	@EqualsAndHashCode.Exclude
	protected Object m_counterLock = new Object();

	@Setter
	protected boolean reset;

	public DoubleGauge(boolean reset)
	{
		this.reset = reset;
	}

	public DoubleGauge()
	{
		this(false);
	}

	@Override
	public void put(double value)
	{
		synchronized (m_counterLock)
		{
			m_gauge = value;
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
		return new DoubleGauge(reset);
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
			metricReporter.put("gauge", new DoubleValue(m_gauge));
			if (reset)
				m_gauge = 0.0;
		}
	}
}
