package org.kairosdb.metrics4j.internal;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class ReportedMetricImpl implements ReportedMetric
{
	private Instant m_time;
	private String m_className;
	private String m_methodName;
	private Map<String, String> m_tags;
	private List<Sample> m_samples = new ArrayList<>();


	public ReportedMetricImpl setTime(Instant time)
	{
		m_time = time;
		return this;
	}

	public String getClassName()
	{
		return m_className;
	}

	public ReportedMetricImpl setClassName(String className)
	{
		m_className = className;
		return this;
	}

	public String getMethodName()
	{
		return m_methodName;
	}


	public ReportedMetricImpl setMethodName(String methodName)
	{
		m_methodName = methodName;
		return this;
	}

	public Map<String, String> getTags()
	{
		return m_tags;
	}

	public ReportedMetricImpl setTags(Map<String, String> tags)
	{
		m_tags = tags;
		return this;
	}

	/**
	 Adds tags that override what was already set.
	 @param tags
	 @return
	 */
	public ReportedMetricImpl addTags(Map<String, String> tags)
	{
		m_tags.putAll(tags);
		return this;
	}

	public ReportedMetricImpl addSample(String fieldName, MetricValue value)
	{
		m_samples.add(new ReportedMetricImpl.SampleImpl(fieldName, value));
		return this;
	}

	public ReportedMetricImpl addSample(String fieldName, MetricValue value, Instant time)
	{
		m_samples.add(new ReportedMetricImpl.SampleImpl(fieldName, value, time));
		return this;
	}

	public List<Sample> getSamples()
	{
		return m_samples;
	}


	@ToString
	@EqualsAndHashCode
	public class SampleImpl implements Sample
	{
		private final String m_fieldName;
		private final MetricValue m_value;
		private final Instant m_time;

		public SampleImpl(String fieldName, MetricValue value)
		{
			m_fieldName = fieldName;
			m_value = value;
			m_time = null;
		}

		public SampleImpl(String fieldName, MetricValue value, Instant time)
		{
			m_fieldName = fieldName;
			m_value = value;
			m_time = time;
		}


		public String getFieldName()
		{
			return m_fieldName;
		}

		public MetricValue getValue()
		{
			return m_value;
		}

		public Instant getTime()
		{
			if (m_time != null)
				return m_time;
			else
				return ReportedMetricImpl.this.m_time;
		}

	}
}
