package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.triggers.MetricCollection;
import org.kairosdb.metrics4j.triggers.Trigger;

import java.time.Instant;

public class TestTrigger implements Trigger
{
	private MetricCollection m_collection;

	@Override
	public void setMetricCollection(MetricCollection collection)
	{
		m_collection = collection;
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	public void triggerCollection(Instant now)
	{
		m_collection.reportMetrics(now);
	}
}
