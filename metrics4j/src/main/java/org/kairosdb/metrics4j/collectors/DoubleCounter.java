package org.kairosdb.metrics4j.collectors;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

@ToString
@EqualsAndHashCode
public class DoubleCounter implements DoubleCollector
{
	private double m_count;

	@EqualsAndHashCode.Exclude
	private Object m_counterLock = new Object();

	@Setter
	private boolean reset;

	public DoubleCounter(boolean reset)
	{
		this.reset = reset;
	}

	public DoubleCounter()
	{
		this(false);
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
	public Collector clone()
	{
		return new DoubleCounter(reset);
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
			metricReporter.put("count", new DoubleValue(m_count));
			if (reset)
				m_count = 0.0;
		}
	}
}
