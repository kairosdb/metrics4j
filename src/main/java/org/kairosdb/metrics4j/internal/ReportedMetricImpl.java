package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.Map;

public class ReportedMetricImpl implements ReportedMetric
{
	private long m_time;
	private String m_metricName;
	private String m_className;
	private String m_methodName;
	private Map<String, String> m_tags;
	private Map<String, MetricValue> m_fields;

	@Override
	public long getTime()
	{
		return m_time;
	}

	@Override
	public ReportedMetric setTime(long time)
	{
		m_time = time;
		return this;
	}

	@Override
	public String getMetricName()
	{
		return m_metricName;
	}

	@Override
	public ReportedMetric setMetricName(String metricName)
	{
		m_metricName = metricName;
		return this;
	}

	@Override
	public String getClassName()
	{
		return m_className;
	}

	@Override
	public ReportedMetric setClassName(String className)
	{
		m_className = className;
		return this;
	}

	@Override
	public String getMethodName()
	{
		return m_methodName;
	}

	@Override
	public ReportedMetric setMethodName(String methodName)
	{
		m_methodName = methodName;
		return this;
	}

	@Override
	public Map<String, String> getTags()
	{
		return m_tags;
	}

	@Override
	public ReportedMetric setTags(Map<String, String> tags)
	{
		m_tags = tags;
		return this;
	}

	@Override
	public Map<String, MetricValue> getFields()
	{
		return m_fields;
	}

	@Override
	public ReportedMetric setFields(Map<String, MetricValue> fields)
	{
		m_fields = fields;
		return this;
	}
}
