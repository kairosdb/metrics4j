package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.PostConstruct;


/**
 All collectors must be thread safe
 */
public interface Collector extends PostConstruct, MetricCollector
{
	/**
	 The first instance of a collector is unmarshalled from configuration using
	 hocon, all other instances are cloned from that first one so the clone
	 method should pass along any configuration that was set.
	 @return
	 */
	Collector clone();
}
