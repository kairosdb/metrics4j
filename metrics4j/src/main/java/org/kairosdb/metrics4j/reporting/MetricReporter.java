package org.kairosdb.metrics4j.reporting;

import java.time.Instant;
import java.util.Map;

public interface MetricReporter
{
	ReportedMetric.Sample put(String field, MetricValue value);
	//void put(String field, MetricValue value, Instant time);

	/**
	 Sets the context for the data collected from the collector.  This can be
	 if the data is a counter or gauge or histogram, event type of unit can be added
	 to the context.  The context must be treated as an immutable map.
	 * @param context  Immutable map of context from the collector.
	 */
	void setContext(Map<String, String> context);

	//void putGauge(String field, MetricValue value);
	//void putGauge(String field, MetricValue value, Instant time);

	//void putSum(String field, MetricValue value);
	//void putSum(String field, MetricValue value, Instant time);

	//void putHistogram()
}
