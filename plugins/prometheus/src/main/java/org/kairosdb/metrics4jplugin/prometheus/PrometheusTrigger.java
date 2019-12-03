package org.kairosdb.metrics4jplugin.prometheus;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.triggers.MetricCollection;
import org.kairosdb.metrics4j.triggers.Trigger;

import java.time.Instant;

public class PrometheusTrigger implements Trigger
{
	private MetricCollection m_collection;

	@Override
	public void setMetricCollection(MetricCollection collection)
	{
		m_collection = collection;
	}

	void reportMetrics()
	{
		m_collection.reportMetrics(Instant.now());
	}

	@Override
	public void init(MetricsContext context)
	{
	}
}
