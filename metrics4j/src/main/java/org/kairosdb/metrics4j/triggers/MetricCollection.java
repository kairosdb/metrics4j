package org.kairosdb.metrics4j.triggers;

import java.time.Instant;

public interface MetricCollection
{
	void reportMetrics(Instant now);
}
