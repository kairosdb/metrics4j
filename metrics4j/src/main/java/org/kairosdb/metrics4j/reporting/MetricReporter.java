package org.kairosdb.metrics4j.reporting;

import java.time.Instant;

public interface MetricReporter
{
	void put(String field, MetricValue value);
	void put(String field, MetricValue value, Instant time);

	//void putGauge(String field, MetricValue value);
	//void putGauge(String field, MetricValue value, Instant time);

	//void putSum(String field, MetricValue value);
	//void putSum(String field, MetricValue value, Instant time);

	//void putHistogram()
}
