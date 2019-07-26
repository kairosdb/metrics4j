package org.kairosdb.metrics4j.reporting;

public interface MetricReporter
{

	void put(String field, MetricValue value);
}
