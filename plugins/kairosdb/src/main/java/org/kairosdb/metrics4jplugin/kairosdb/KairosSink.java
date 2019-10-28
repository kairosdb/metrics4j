package org.kairosdb.metrics4jplugin.kairosdb;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;

import java.util.List;

public class KairosSink implements MetricSink
{
	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{

	}

	@Override
	public void init(MetricsContext context)
	{
		
	}
}
