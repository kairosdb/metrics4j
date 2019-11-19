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
public class FormattedMetric implements ReportedMetric
{
	private final ReportedMetric m_metric;
	private final List<Sample> m_samples;

	public FormattedMetric(ReportedMetric metric)
	{
		m_metric = metric;
		m_samples = new ArrayList<>();
	}

	public void addSample(Sample sample, String formattedName)
	{
		m_samples.add(new FormattedSample(sample, formattedName));
	}

	@Override
	public String getMetricName()
	{
		return m_metric.getMetricName();
	}

	@Override
	public String getClassName()
	{
		return m_metric.getClassName();
	}

	@Override
	public String getMethodName()
	{
		return m_metric.getMethodName();
	}

	@Override
	public Map<String, String> getTags()
	{
		return m_metric.getTags();
	}

	@Override
	public Map<String, String> getProps()
	{
		return m_metric.getProps();
	}

	@Override
	public List<Sample> getSamples()
	{
		return m_samples;
	}



	@ToString
	@EqualsAndHashCode
	private static class FormattedSample implements Sample
	{
		private final Sample m_sample;
		private final String m_metricName;

		private FormattedSample(Sample sample, String metricName)
		{
			m_sample = sample;
			m_metricName = metricName;
		}

		@Override
		public String getFieldName()
		{
			return m_sample.getFieldName();
		}

		@Override
		public MetricValue getValue()
		{
			return m_sample.getValue();
		}

		@Override
		public Instant getTime()
		{
			return m_sample.getTime();
		}

		@Override
		public String getMetricName()
		{
			return m_metricName;
		}
	}
}
