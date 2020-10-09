package org.kairosdb.metrics4j.collectors.impl;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.TimeCollector;
import org.kairosdb.metrics4j.collectors.helpers.ChainedCollector;
import org.kairosdb.metrics4j.configuration.ConfigurationException;

import java.time.Instant;

public class ChainedTimeCollector extends ChainedCollector<TimeCollector> implements TimeCollector
{
	@Override
	public void put(Instant time)
	{
		for (PrefixMetricReporter<TimeCollector> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.getCollector().put(time);
		}
	}

	@Override
	public Collector clone()
	{
		ChainedTimeCollector ret = (ChainedTimeCollector)super.clone();

		return ret;
	}

	@Override
	public TimeCollector validateCollector(Collector collector)
	{
		if (!(collector instanceof TimeCollector))
			throw new ConfigurationException("Collector specified in chain collector configuration is not an instance of a TimeCollector");

		return (TimeCollector)collector;
	}
}
