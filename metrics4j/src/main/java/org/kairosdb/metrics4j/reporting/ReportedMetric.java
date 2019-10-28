package org.kairosdb.metrics4j.reporting;

import java.time.Instant;
import java.util.Map;

public interface ReportedMetric
{
	Instant getTime();

	ReportedMetric setTime(Instant time);

	String getMetricName();

	ReportedMetric setMetricName(String name);

	String getClassName();

	ReportedMetric setClassName(String className);

	String getMethodName();

	ReportedMetric setMethodName(String methodName);

	Map<String, String> getTags();

	ReportedMetric setTags(Map<String, String> tags);

	String getFieldName();

	ReportedMetric setFieldName(String name);

	MetricValue getValue();

	ReportedMetric setValue(MetricValue value);

	Map<String, String> getProps();

	ReportedMetric setProps(Map<String, String> props);
}
