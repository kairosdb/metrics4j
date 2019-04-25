package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.reporting.ReportedMetric;

public interface ReportableMetric
{
	void reportMetric(ReportedMetric reportedMetric);
}
