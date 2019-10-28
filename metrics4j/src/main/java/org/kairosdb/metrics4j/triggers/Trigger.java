package org.kairosdb.metrics4j.triggers;

import org.kairosdb.metrics4j.PostConstruct;

public interface Trigger extends PostConstruct
{
	void setMetricCollection(MetricCollection collection);
}
