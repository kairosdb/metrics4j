package org.kairosdb.metrics4j.collectors;


import java.time.Instant;

/**
 Interface for collecting wall clock time values
 */
public interface TimeCollector extends Collector
{
	void put(Instant time);
}
