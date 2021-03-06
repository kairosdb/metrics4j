package org.kairosdb.metrics4j.collectors.impl;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.helpers.ChainedCollector;
import org.kairosdb.metrics4j.configuration.ConfigurationException;


public class ChainedLongCollector extends ChainedCollector<LongCollector> implements LongCollector
{
	@Override
	public void put(long value)
	{
		for (PrefixMetricReporter<LongCollector> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.getCollector().put(value);
		}
	}

	@Override
	public Collector clone()
	{
		ChainedLongCollector ret = (ChainedLongCollector)super.clone();

		return ret;
	}

	@Override
	public LongCollector validateCollector(Collector collector)
	{
		if (!(collector instanceof LongCollector))
			throw new ConfigurationException("Collector specified in chain collector configuration is not an instance of a LongCollector");

		return (LongCollector)collector;
	}
}
