package org.kairosdb.metrics4j.formatters;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.configuration.ConfigurationException;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@XmlRootElement(name = "formatter")
public class TemplateFormatter implements Formatter
{
	private List<TemplateFragment> m_templateFragmentList = new ArrayList<>();
	@XmlAttribute(name = "template")
	private String m_template;

	public TemplateFormatter()
	{
	}

	/* used for testing */
	TemplateFormatter(String template)
	{
		m_template = template;
	}

	@Override
	public void init(MetricsContext context)
	{
		Pattern pattern = Pattern.compile("\\$\\{([^\\}]*)\\}");

		Matcher matcher = pattern.matcher(m_template);

		int endLastMatch = 0;
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();

			if (start != endLastMatch)
			{
				m_templateFragmentList.add(new StaticTemplateFragment(m_template.substring(endLastMatch, start)));
			}

			String tag = matcher.group(1);

			if ("className".equals(tag))
			{
				m_templateFragmentList.add(new PropertyTemplateFragment(ReportedMetric::getClassName));
			}
			else if ("methodName".equals(tag))
			{
				m_templateFragmentList.add(new PropertyTemplateFragment(ReportedMetric::getMethodName));
			}
			else if (tag.startsWith("tag."))
			{
				m_templateFragmentList.add(new TagTemplateFragment(tag.substring(4)));
			}
			else if ("field".equals(tag))
			{
				m_templateFragmentList.add(new FieldTemplateFragment());
			}
			else
			{
				throw new ConfigurationException("Unrecognized template parameter: "+tag);
			}

			endLastMatch = end;
		}

		if (endLastMatch != m_template.length())
		{
			m_templateFragmentList.add(new StaticTemplateFragment(m_template.substring(endLastMatch)));
		}
	}

	@Override
	public void formatReportedMetric(ReportedMetric reportedMetric)
	{
		StringBuilder sb = new StringBuilder();
		for (TemplateFragment templateFragment : m_templateFragmentList)
		{
			templateFragment.append(sb, reportedMetric);
		}

		reportedMetric.setMetricName(sb.toString());
	}

	private interface TemplateFragment
	{
		void append(StringBuilder sb, ReportedMetric reportedMetric);
	}

	private static class StaticTemplateFragment implements TemplateFragment
	{
		private String m_fragment;

		public StaticTemplateFragment(String string)
		{
			m_fragment = string;
		}

		@Override
		public void append(StringBuilder sb, ReportedMetric reportedMetric)
		{
			sb.append(m_fragment);
		}
	}

	private static class PropertyTemplateFragment implements TemplateFragment
	{
		private final Function<ReportedMetric, String> m_property;

		public PropertyTemplateFragment(Function<ReportedMetric, String> property)
		{
			m_property = property;
		}

		@Override
		public void append(StringBuilder sb, ReportedMetric reportedMetric)
		{
			sb.append(m_property.apply(reportedMetric));
		}
	}

	private static class TagTemplateFragment implements TemplateFragment
	{
		private final String m_tag;

		public TagTemplateFragment(String tag)
		{
			m_tag = tag;
		}

		@Override
		public void append(StringBuilder sb, ReportedMetric reportedMetric)
		{
			String tagValue = reportedMetric.getTags().get(m_tag);
			if (tagValue != null)
				sb.append(tagValue);
		}
	}

	private static class FieldTemplateFragment implements TemplateFragment
	{
		@Override
		public void append(StringBuilder sb, ReportedMetric reportedMetric)
		{
			sb.append(reportedMetric.getFieldName());
		}
	}
}
