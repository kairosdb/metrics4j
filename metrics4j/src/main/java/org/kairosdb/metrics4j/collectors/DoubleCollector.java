package org.kairosdb.metrics4j.collectors;

public interface DoubleCollector extends Collector
{
	void put(double value);
}
