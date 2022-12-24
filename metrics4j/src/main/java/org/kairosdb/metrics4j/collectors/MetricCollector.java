package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.io.Serializable;
import java.util.Map;

public interface MetricCollector extends Serializable
{
	void reportMetric(MetricReporter metricReporter);

	void setContextProperties(Map<String, String> contextProperties);
}
