package org.kairosdb.metrics4j.collectors.impl;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;
import org.kairosdb.metrics4j.collectors.helpers.ChainedCollector;
import org.kairosdb.metrics4j.collectors.helpers.SystemTicker;
import org.kairosdb.metrics4j.collectors.helpers.Ticker;
import org.kairosdb.metrics4j.configuration.ConfigurationException;

import java.time.Duration;
import java.util.concurrent.Callable;

public class ChainedDurationCollector extends ChainedCollector<DurationCollector> implements DurationCollector
{
	private final Ticker m_ticker = new SystemTicker();

	@Override
	public void put(Duration duration)
	{
		for (PrefixMetricReporter<DurationCollector> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.getCollector().put(duration);
		}
	}

	@Override
	public Collector clone()
	{
		ChainedDurationCollector ret = (ChainedDurationCollector)super.clone();

		return ret;
	}

	@Override
	public <T> T time(Callable<T> callable) throws Exception
	{
		try (BlockTimer timer = time())
		{
			return callable.call();
		}
	}

	@Override
	public BlockTimer time()
	{
		return new BlockTimer(this, m_ticker);
	}

	@Override
	public DurationCollector validateCollector(Collector collector)
	{
		if (!(collector instanceof DurationCollector))
			throw new ConfigurationException("Collector specified in chain collector configuration is not an instance of a DurationCollector");

		return (DurationCollector)collector;
	}
}
