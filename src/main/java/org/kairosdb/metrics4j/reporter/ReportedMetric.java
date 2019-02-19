package org.kairosdb.metrics4j.reporter;

import java.util.Map;

public interface ReportedMetric
{
	long getTime();

	ReportedMetric setTime(long time);

	String getMetricName();

	ReportedMetric setMetricName(String metricName);

	String getClassName();

	ReportedMetric setClassName(String className);

	String getMethodName();

	ReportedMetric setMethodName(String methodName);

	Map<String, String> getTags();

	ReportedMetric setTags(Map<String, String> tags);

	Map<String, MetricValue> getFields();

	ReportedMetric setFields(Map<String, MetricValue> fields);
}
