package org.kairosdb.metrics4j.formatters;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

public class DefaultFormatter implements Formatter
{
	public final String m_separator;

	public DefaultFormatter()
	{
		m_separator = ".";
	}

	public DefaultFormatter(String separator)
	{
		m_separator = separator;
	}

	@Override
	public String formatReportedMetric(ReportedMetric reportedMetric, ReportedMetric.Sample sample)
	{
		StringBuilder sb = new StringBuilder();

		if (reportedMetric.getMetricName() != null)
		{
			sb.append(reportedMetric.getMetricName()).append(m_separator).append(sample.getFieldName());
		}
		else
		{
			sb.append(reportedMetric.getClassName()).append(m_separator)
					.append(reportedMetric.getMethodName()).append(m_separator)
					.append(sample.getFieldName());
		}

		return sb.toString();
	}

	@Override
	public void init(MetricsContext context)
	{
	}
}
