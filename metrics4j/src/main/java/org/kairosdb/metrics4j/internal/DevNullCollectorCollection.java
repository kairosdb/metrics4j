package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.impl.NullCollector;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Instant;
import java.util.Collections;

public class DevNullCollectorCollection implements CollectorCollection
{
	private static final Collector COLLECTOR = new NullCollector();

	@Override
	public Collector getCollector(TagKey tagKey)
	{
		return COLLECTOR;
	}

	@Override
	public Iterable<ReportedMetric> gatherMetrics(Instant now)
	{
		return Collections.emptyList();
	}
}
