package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.PostConstruct;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.List;

public interface MetricSink extends PostConstruct
{
	void reportMetrics(List<FormattedMetric> metrics);
	Formatter getDefaultFormatter();
}
