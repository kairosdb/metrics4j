package org.kairosdb.metrics4j.stats;

import org.kairosdb.metrics4j.reporter.ReportedMetric;

public interface ReportableStats
{
	void reportMetrics(ReportedMetric reportedMetric);
}
