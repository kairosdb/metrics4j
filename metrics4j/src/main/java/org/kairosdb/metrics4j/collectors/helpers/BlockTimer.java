package org.kairosdb.metrics4j.collectors.helpers;

import org.kairosdb.metrics4j.collectors.DurationCollector;

import java.time.Duration;

public class BlockTimer implements AutoCloseable
{
	private final DurationCollector m_collector;
	private final Ticker m_ticker;
	private final long m_start;

	public BlockTimer(DurationCollector collector, Ticker ticker)
	{
		m_collector = collector;
		m_ticker = ticker;
		m_start = m_ticker.read();
	}

	/**
	 Stops timer and reports duration recorded
	 */
	@Override
	public void close()
	{
		m_collector.put(Duration.ofNanos(m_ticker.read() - m_start));
	}
}
