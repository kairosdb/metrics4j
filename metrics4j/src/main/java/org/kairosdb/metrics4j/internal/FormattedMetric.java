package org.kairosdb.metrics4j.internal;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ToString
@EqualsAndHashCode
public class FormattedMetric
{
	private final ReportedMetric m_metric;
	private final List<Sample> m_samples;
	private final Map<String, String> m_props;
	private final Map<String, String> m_combinedTags;
	private final String m_help;

	public FormattedMetric(ReportedMetric metric, Map<String, String> props, Map<String, String> staticTags, String help)
	{
		m_metric = metric;
		m_samples = new ArrayList<>();
		m_props = props;
		m_combinedTags = new MapCombiner<>(staticTags, m_metric.getTags());
		m_help = help;
	}

	public void addSample(ReportedMetric.Sample sample, String formattedName)
	{
		m_samples.add(new Sample(sample, formattedName));
	}

	public ReportedMetric getMetric()
	{
		return m_metric;
	}

	public String getClassName()
	{
		return m_metric.getClassName();
	}

	public String getMethodName()
	{
		return m_metric.getMethodName();
	}

	public Map<String, String> getTags()
	{
		return m_combinedTags;
	}

	public List<Sample> getSamples()
	{
		return m_samples;
	}

	public Map<String, String> getProps()
	{
		return m_props;
	}

	public String getHelp()
	{
		return m_help;
	}

	/**
	 Returns context specific to this data point.  The returned map must be considered
	 immutable.  Do not try to modify it.
	 * @return
	 */
	public Map<String, String> getContext()
	{
		return m_metric.getContext();
	}



	//We have to wrap each sample per sink
	@ToString
	@EqualsAndHashCode
	public static class Sample
	{
		private final ReportedMetric.Sample m_sample;
		private final String m_metricName;

		private Sample(ReportedMetric.Sample sample, String metricName)
		{
			m_sample = sample;
			m_metricName = metricName;
		}

		public String getFieldName()
		{
			return m_sample.getFieldName();
		}

		public MetricValue getValue()
		{
			return m_sample.getValue();
		}

		public Instant getTime()
		{
			return m_sample.getTime();
		}

		public String getMetricName()
		{
			return m_metricName;
		}
	}
}
