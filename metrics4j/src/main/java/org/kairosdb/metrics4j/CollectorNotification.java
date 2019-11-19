package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.collectors.Collector;

public interface CollectorNotification
{
	void newCollector(String name, Collector collector);
}
