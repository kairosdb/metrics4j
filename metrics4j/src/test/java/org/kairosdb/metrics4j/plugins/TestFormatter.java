package org.kairosdb.metrics4j.plugins;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

public class TestFormatter implements Formatter
{
	@Override
	public void init(MetricsContext context)
	{
	}

	@Override
	public String formatReportedMetric(ReportedMetric reportedMetric, ReportedMetric.Sample sample, String metricName)
	{
		return reportedMetric.getClassName()+"."+
				reportedMetric.getMethodName()+"."+
				sample.getFieldName();
	}
}
