package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.internal.TagKey;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;


/**
 A set of collectors, typically this is a set of collectors for the same
 method call but with different tags.
 */
public interface CollectorCollection
{
	//return a collector specific to the args passed, maybe change to set of string or map of tags.
	MetricCollector getCollector(TagKey tagKey);

	Iterable<ReportedMetric> gatherMetrics(Instant now);
}
