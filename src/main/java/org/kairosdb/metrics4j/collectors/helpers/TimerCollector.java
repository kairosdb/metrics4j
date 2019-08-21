package org.kairosdb.metrics4j.collectors.helpers;

import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;

import java.util.concurrent.Callable;

public abstract class TimerCollector implements DurationCollector
{
	private final Ticker m_ticker = new SystemTicker();

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

	public abstract Collector clone();

}
