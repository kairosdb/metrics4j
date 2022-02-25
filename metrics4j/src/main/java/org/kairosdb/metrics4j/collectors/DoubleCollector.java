package org.kairosdb.metrics4j.collectors;

import java.time.Instant;

public interface DoubleCollector extends Collector
{
	/**
	 Place a double value into the collector to be reported.
	 The actual value reported is determined by the collector implementation
	 that is configured for this source.
	 @param value Value to be reported.
	 */
	void put(double value);

	/**
	 Place a double value and associated timestamp into the collector to be reported.
	 The actual value and time reported is determined by the collector implementation
	 that is configured for this source.
	 @param time Suggested timestamp to use when reporting.
	 @param value Value to be reported.
	 */
	void put(Instant time, double value);
}
