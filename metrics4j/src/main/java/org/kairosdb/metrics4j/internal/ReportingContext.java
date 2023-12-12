package org.kairosdb.metrics4j.internal;

/**
 This is a place to define context that is added to a metric by the collector
 at reporting time.
 */
public interface ReportingContext
{
	/**
	 For collectors that report a duration this will be set.  Values are
	 the enum values from ChronoUnit class in java.
	 */
	String CHRONO_UNIT_KEY = "chrono_unit";

	String TYPE_KEY = "type";  //gauge or counter, maybe historgram
	String TYPE_GAUGE_VALUE = "gauge";
	String TYPE_COUNTER_VALUE = "counter";
	String TYPE_SUMMARY_VALUE = "summary";
	String TYPE_STRING_VALUE = "string";

	String AGGREGATION_KEY = "aggregation";
	String AGGREGATION_CUMULATIVE_VALUE = "cumulative";
	String AGGREGATION_DELTA_VALUE = "delta";


}
