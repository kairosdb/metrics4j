package org.kairosdb.metrics4j.collectors;

import java.time.Duration;

public interface DurationCollector extends Collector
{
	void put(Duration duration);
}
