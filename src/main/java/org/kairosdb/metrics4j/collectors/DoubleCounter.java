package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.Collections;

public class DoubleCounter implements DoubleCollector, ReportableMetric
{
	private double m_count;
	private Object m_counterLock = new Object();

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
		return new DoubleCounter();
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(ReportedMetric reportedMetric)
	{
		synchronized (m_counterLock)
		{
			reportedMetric.setFields(Collections.singletonMap("count", new DoubleValue(m_count)));
		}
	}
}
