package org.kairosdb.metrics4j.collectors;

public interface LongCollector extends Collector
{
	void put(long value);
}
