package org.kairosdb.metrics4j.collectors.helpers;

import lombok.Setter;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Callable;


public abstract class TimerCollector implements DurationCollector
{
	private final Ticker m_ticker = new SystemTicker();

	/**
	 Unit to report metric as.  Supported units are NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, DAYS
	 */
	//todo add validation
	@Setter
	protected ChronoUnit reportUnit = ChronoUnit.MILLIS;

	protected long getValue(Duration duration)
	{
		switch (reportUnit)
		{
			case NANOS: return duration.toNanos();
			case MICROS: return duration.toNanos() / 1000;
			case MILLIS: return duration.toMillis();
			case SECONDS: return duration.getSeconds();
			case MINUTES: return duration.toMinutes();
			case HOURS: return duration.toHours();
			case DAYS: return duration.toDays();
			default: return 0;
		}
	}

	@Override
	public <T> T timeEx(Callable<T> callable) throws Exception
	{
		try (BlockTimer ignored = time())
		{
			return callable.call();
		}
	}

	@Override
	public <T> T time(TimeCallable<T> callable)
	{
		try (BlockTimer ignored = time())
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
