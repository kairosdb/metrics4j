package org.kairosdb.metrics4j.collectors;

public interface StringCollector extends Collector
{
	void put(String value);
}
