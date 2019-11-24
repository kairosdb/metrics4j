package org.kairosdb.metrics4j.reporting;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface ReportedMetric
{
	String getClassName();

	String getMethodName();

	Map<String, String> getTags();

	List<ReportedMetric.Sample> getSamples();


	interface Sample
	{
		String getFieldName();

		MetricValue getValue();

		Instant getTime();
	}
}
