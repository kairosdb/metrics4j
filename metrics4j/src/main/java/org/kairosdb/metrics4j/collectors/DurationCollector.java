package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;

import java.time.Duration;
import java.util.concurrent.Callable;

public interface DurationCollector extends Collector
{
	void put(Duration duration);
	<T> T time(Callable<T> callable) throws Exception;
	BlockTimer time();
}
