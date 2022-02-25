package org.kairosdb.metrics4j.collectors.impl;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.collectors.helpers.ChainedCollector;
import org.kairosdb.metrics4j.configuration.ConfigurationException;

import java.time.Instant;

public class ChainedStringCollector extends ChainedCollector<StringCollector> implements StringCollector
{
	@Override
	public void put(String value)
	{
		for (PrefixMetricReporter<StringCollector> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.getCollector().put(value);
		}
	}

	@Override
	public void put(Instant time, String value)
	{
		for (PrefixMetricReporter<StringCollector> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.getCollector().put(time, value);
		}
	}

	@Override
	public Collector clone()
	{
		ChainedStringCollector ret = (ChainedStringCollector)super.clone();

		return ret;
	}

	@Override
	public StringCollector validateCollector(Collector collector)
	{
		if (!(collector instanceof StringCollector))
			throw new ConfigurationException("Collector specified in chain collector configuration is not an instance of a StringCollector");

		return (StringCollector)collector;
	}
}
