package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

public class TestFormatter implements Formatter
{
	@Override
	public void formatReportedMetric(ReportedMetric reportedMetric)
	{
		reportedMetric.setMetricName(
				reportedMetric.getClassName()+"."+
						reportedMetric.getMethodName()+"."+
						reportedMetric.getFieldName());
	}

	@Override
	public void init(MetricsContext context)
	{
	}
}
