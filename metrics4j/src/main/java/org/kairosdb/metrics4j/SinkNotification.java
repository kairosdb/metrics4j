package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.sinks.MetricSink;

public interface SinkNotification
{
	void newSink(String name, MetricSink component);
}
