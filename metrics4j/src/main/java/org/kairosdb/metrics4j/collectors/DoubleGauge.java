package org.kairosdb.metrics4j.collectors;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;


@ToString
@EqualsAndHashCode
public class DoubleGauge implements DoubleCollector
{
	private double m_gauge = 0.0;
	private Object m_counterLock = new Object();

	@Setter
	private boolean reset;

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
