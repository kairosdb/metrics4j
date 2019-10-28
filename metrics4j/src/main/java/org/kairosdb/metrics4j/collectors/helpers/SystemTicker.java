package org.kairosdb.metrics4j.collectors.helpers;

public class SystemTicker implements Ticker
{
	@Override
	public long read()
	{
		return System.nanoTime();
	}
}
