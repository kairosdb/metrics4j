package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.io.Serializable;

public interface ReportableMetric extends Serializable
{
	void reportMetric(ReportedMetric reportedMetric);
}
