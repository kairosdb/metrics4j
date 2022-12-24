package org.kairosdb.metrics4j.formatters;

import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.Arrays;

public class MethodToSnakeCase extends TemplateFormatter
{
	public MethodToSnakeCase()
	{
		super();
	}

	public MethodToSnakeCase(String template)
	{
		super(template);
	}

	@Override
	public String formatReportedMetric(ReportedMetric reportedMetric, ReportedMetric.Sample sample, String metricName)
	{
		if (metricName == null)
		{
			String methodName = reportedMetric.getMethodName();
			StringBuilder sb = new StringBuilder();
			boolean firstChar = true;
			for (char c : methodName.toCharArray())
			{
				if (firstChar)
				{
					sb.append(Character.toLowerCase(c));
					firstChar = false;
				}
				else if (Character.isUpperCase(c))
					sb.append('_').append(Character.toLowerCase(c));
				else
					sb.append(c);
			}

			metricName = sb.toString();
		}

		return super.formatReportedMetric(reportedMetric, sample, metricName);
	}
}
