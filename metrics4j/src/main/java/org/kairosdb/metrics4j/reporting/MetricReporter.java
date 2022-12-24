package org.kairosdb.metrics4j.reporting;

import java.time.Instant;

public interface MetricReporter
{
	void put(String field, MetricValue value);
	void put(String field, MetricValue value, Instant time);
}
