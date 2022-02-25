package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;

/**
 Interface for reporting time durations.  It contains helper methods for
 measuring how long code blocks take to run
 */
public interface DurationCollector extends Collector
{
	/**
	 Place a Duration value into the collector to be reported.
	 The actual value reported is determined by the collector implementation
	 that is configured for this source.
	 @param duration Value to be reported.
	 */
	void put(Duration duration);

	/**
	 Place a Duration value and associated timestamp into the collector to be reported.
	 The actual value and time reported is determined by the collector implementation
	 that is configured for this source.
	 @param time Suggested timestamp to use when reporting.
	 @param duration Value to be reported.
	 */
	void put(Instant time, Duration duration);
	<T> T timeEx(Callable<T> callable) throws Exception;
	<T> T time(TimeCallable<T> callable);
	BlockTimer time();

	interface TimeCallable<T>
	{
		T call();
	}
}
