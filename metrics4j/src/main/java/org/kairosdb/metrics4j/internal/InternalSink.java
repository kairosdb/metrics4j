package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.reporting.ReportedMetric;

public interface InternalSink
{
	void addMetric(ReportedMetric reportedMetric);
}
