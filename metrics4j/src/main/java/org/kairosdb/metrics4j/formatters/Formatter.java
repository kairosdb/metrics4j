package org.kairosdb.metrics4j.formatters;

import org.kairosdb.metrics4j.PostConstruct;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

public interface Formatter extends PostConstruct
{
	String formatReportedMetric(ReportedMetric reportedMetric, ReportedMetric.Sample sample, String metricName);
}
