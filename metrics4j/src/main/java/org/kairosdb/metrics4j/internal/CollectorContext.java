package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 Wraps a collector and associates it with a formatter and a list of sinks to send
 metrics to.  Also contains static tags to add to metric.
 */
public interface CollectorContext
{
	CollectorCollection getCollection();
	Map<String, String> getTags();
	List<SinkQueue> getSinkQueueList();
	void reportMetrics(Instant now);

}
