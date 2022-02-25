package org.kairosdb.metrics4j.collectors.impl;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.helpers.ChainedCollector;
import org.kairosdb.metrics4j.configuration.ConfigurationException;

import java.time.Instant;


public class ChainedDoubleCollector extends ChainedCollector<DoubleCollector> implements DoubleCollector
{
	@Override
	public void put(double value)
	{
		for (PrefixMetricReporter<DoubleCollector> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.getCollector().put(value);
		}
	}

	@Override
	public void put(Instant time, double value)
	{
		for (PrefixMetricReporter<DoubleCollector> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.getCollector().put(time, value);
		}
	}

	@Override
	public Collector clone()
	{
		ChainedDoubleCollector ret = (ChainedDoubleCollector)super.clone();

		return ret;
	}

	@Override
	public DoubleCollector validateCollector(Collector collector)
	{
		if (!(collector instanceof DoubleCollector))
			throw new ConfigurationException("Collector specified in chain collector configuration is not an instance of a DoubleCollector");

		return (DoubleCollector)collector;
	}
}
