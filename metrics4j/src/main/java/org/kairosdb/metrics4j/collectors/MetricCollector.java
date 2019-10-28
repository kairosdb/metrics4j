package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.io.Serializable;

public interface MetricCollector extends Serializable
{
	void reportMetric(MetricReporter metricReporter);
}
