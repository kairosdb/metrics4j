package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

@ToString
@EqualsAndHashCode
public class LastTime extends TimerCollector implements DurationCollector, MetricCollector
{
	protected AtomicReference<Duration> m_lastTime = new AtomicReference<>();

	@Override
	public Collector clone()
	{
		LastTime ret = new LastTime();
		ret.reportUnit = reportUnit;

		return ret;
	}

	@Override
	public void init(MetricsContext context)
	{
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		Duration lastTime = m_lastTime.getAndSet(null);

		if (lastTime != null)
		{
			metricReporter.put("value", new LongValue(getValue(lastTime)));
		}

	}

	@Override
	public void put(Duration duration)
	{
		m_lastTime.set(duration);
	}

	@Override
	public void put(Instant time, Duration duration)
	{
		put(duration);
	}


}
