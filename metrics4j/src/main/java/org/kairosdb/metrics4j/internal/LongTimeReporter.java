package org.kairosdb.metrics4j.internal;

import lombok.EqualsAndHashCode;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricValue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.ToLongFunction;

@EqualsAndHashCode
public class LongTimeReporter implements TimeReporter
{
	private ToLongFunction<Duration> m_toLongFunction;

	public LongTimeReporter(ChronoUnit reportUnit)
	{

		switch (reportUnit)
		{
			case NANOS:
				m_toLongFunction = value -> value.toNanos();
				break;
			case MICROS:
				m_toLongFunction = value -> value.toNanos() / 1000;
				break;
			case MILLIS:
				m_toLongFunction = value -> value.toMillis();
				break;
			case SECONDS:
				m_toLongFunction = value -> value.getSeconds();
				break;
			case MINUTES:
				m_toLongFunction = value -> value.toMinutes();
				break;
			case HOURS:
				m_toLongFunction = value -> value.toHours();
				break;
			case DAYS:
				m_toLongFunction = value -> value.toDays();
				break;
			default:
				m_toLongFunction = value -> 0L;
		}
	}

	@Override
	public MetricValue getValue(Duration duration)
	{
		return new LongValue(m_toLongFunction.applyAsLong(duration));
	}
}
