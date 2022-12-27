package org.kairosdb.metrics4j.internal;

import lombok.EqualsAndHashCode;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricValue;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.ToDoubleFunction;

@EqualsAndHashCode
public class DoubleTimeReporter implements TimeReporter
{
	private ToDoubleFunction<Duration> m_toDoubleFunction;

	public DoubleTimeReporter(ChronoUnit reportUnit)
	{
		switch (reportUnit)
		{
			case NANOS:
				m_toDoubleFunction = value -> value.toNanos();
				break;
			case MICROS:
				m_toDoubleFunction = value -> value.toNanos() / 1000.0;
				break;
			case MILLIS:
				m_toDoubleFunction = value -> (value.toNanos() / 1000) / 1000.0;
				break;
			case SECONDS:
				m_toDoubleFunction = value -> value.toMillis() / 1000.0;
				break;
			case MINUTES:
				m_toDoubleFunction = value -> (value.toMillis() / 60) / 1000.0;
				break;
			case HOURS:
				m_toDoubleFunction = value -> (value.toMillis() / 60 / 60) / 1000.0;
				break;
			case DAYS:
				m_toDoubleFunction = value -> (value.toMillis() / 60 / 60 / 24) / 1000.0;
				break;
			default:
				m_toDoubleFunction = value -> 0L;
		}
	}

	@Override
	public MetricValue getValue(Duration duration)
	{
		return new DoubleValue(m_toDoubleFunction.applyAsDouble(duration));
	}
}
