package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.TimeCollector;
import org.kairosdb.metrics4j.util.Clock;

import java.time.Duration;
import java.time.Instant;

/**
 This records the difference between now (computers local clock) and the timestamp
 provided.  The deltas are recorded as a SimpleTimerMetric
 */
@ToString
@EqualsAndHashCode
public class TimeDelta extends SimpleTimerMetric implements TimeCollector
{
	private final Clock m_clock;

	public TimeDelta(Clock clock)
	{
		m_clock = clock;
	}

	public TimeDelta()
	{
		this(new Clock());
	}


	@Override
	public void put(Instant time)
	{
		Duration between = Duration.between(time, Instant.ofEpochMilli(m_clock.now())).abs();
		super.put(between);
	}

	@Override
	public Collector clone()
	{
		TimeDelta ret = new TimeDelta();
		ret.reportUnit = reportUnit;
		ret.reportZero = reportZero;

		return ret;
	}

	@Override
	public void init(MetricsContext context)
	{
		super.init(context);
	}
}
