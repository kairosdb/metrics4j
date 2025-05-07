package org.kairosdb.metrics4j.collectors.helpers;

import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.collectors.TimeCollector;

public interface AllCollectors extends LongCollector, DoubleCollector,
		StringCollector, DurationCollector, TimeCollector, MetricCollector
{
}
