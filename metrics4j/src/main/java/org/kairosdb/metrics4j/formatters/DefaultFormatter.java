package org.kairosdb.metrics4j.formatters;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

public class DefaultFormatter implements Formatter
{
	public final String m_separator;
	public final boolean m_replaceDot;

	public DefaultFormatter()
	{
		this(".");
	}

	public DefaultFormatter(String separator)
	{
		m_separator = separator;
		m_replaceDot = (!m_separator.equals("."));
	}

	@Override
	public String formatReportedMetric(ReportedMetric reportedMetric, ReportedMetric.Sample sample, String metricName)
	{
		StringBuilder sb = new StringBuilder();

		if (metricName != null)
		{
			sb.append(metricName).append(m_separator).append(sample.getFieldName());
		}
		else
		{
			String className = reportedMetric.getClassName();
			if (m_replaceDot)
			{
				className = className.replace(".", m_separator);
			}

			sb.append(className).append(m_separator)
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
