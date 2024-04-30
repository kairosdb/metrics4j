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

	Map<String, String> getContext();


	interface Sample
	{
		String getFieldName();

		MetricValue getValue();

		Instant getTime();

		Sample setTime(Instant time);

		/**
		 Sets context for this sample.  The object set is determined
		 by the type of metric that is being reported.  See ReportingContext
		 * @param obj Context to set
		 * @return This reference
		 */
		Sample setSampleContext(Object obj);

		Object getSampleContext();
	}
}
