package org.kairosdb.metrics4j.reporting;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface ReportedMetric
{
	/**
	 This is set only if the user has provided a metric name in the source configuration.
	 @return
	 */
	String getMetricName();

	String getClassName();

	String getMethodName();

	Map<String, String> getTags();

	//ReportedMetric setTags(Map<String, String> tags);

	Map<String, String> getProps();

	//ReportedMetric setProps(Map<String, String> props);

	/**
	 Used to set time for this set of samples.  The time may be overridden by
	 individual samples
	 @param time
	 @return
	 */
	//ReportedMetric setTime(Instant time);

	List<Sample> getSamples();


	interface Sample
	{
		String getFieldName();
		MetricValue getValue();
		Instant getTime();

		String getMetricName();
		//void setMetricName(String name);
	}
}
