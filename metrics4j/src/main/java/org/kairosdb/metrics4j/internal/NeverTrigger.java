package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.triggers.MetricCollection;
import org.kairosdb.metrics4j.triggers.Trigger;

public class NeverTrigger implements Trigger
{
	@Override
	public void setMetricCollection(MetricCollection collection)
	{

	}

	@Override
	public void init(MetricsContext context)
	{

	}
}
