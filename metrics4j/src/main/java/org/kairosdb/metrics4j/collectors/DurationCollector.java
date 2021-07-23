package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 Interface for reporting time durations.  It contains helper methods for
 measuring how long code blocks take to run
 */
public interface DurationCollector extends Collector
{
	void put(Duration duration);
	<T> T timeEx(Callable<T> callable) throws Exception;
	<T> T time(TimeCallable<T> callable);
	BlockTimer time();

	interface TimeCallable<T>
	{
		T call();
	}
}
