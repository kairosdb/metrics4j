package org.kairosdb.metrics4jplugin.opentelemetry;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_SUMMARY_VALUE;

public class OtelFormatter implements Formatter
{
	public final String m_separator;
	public final boolean m_replaceDot;

	public OtelFormatter()
	{
		this(".");
	}

	public OtelFormatter(String separator)
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
			sb.append(metricName);
		}
		else
		{
			String className = reportedMetric.getClassName();
			if (m_replaceDot)
			{
				className = className.replace(".", m_separator);
			}

			sb.append(className).append(m_separator)
					.append(reportedMetric.getMethodName());
		}


		//Append the field name only if it is not a summary type
		if (!TYPE_SUMMARY_VALUE.equals(reportedMetric.getContext().get(TYPE_KEY)))
		{
			sb.append(m_separator).append(sample.getFieldName());
		}

		return sb.toString();
	}

	@Override
	public void init(MetricsContext context)
	{
	}
}
