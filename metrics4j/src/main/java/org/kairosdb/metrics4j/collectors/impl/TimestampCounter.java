package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.TimeCollector;
import org.kairosdb.metrics4j.collectors.helpers.Cloneable;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.util.Clock;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_CUMULATIVE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;

/**
 This collector records a count of timestamps during a configurable amount of time.
 The initial use of this collector was to count events going in and out of an event
 system.  The idea is to count all events that occur in a bucket of time and report
 them using a timestamp within that bucket.

 The default configuration can handle the same timestamp (rounded to the minute)
 being reported for just short of 2 weeks (13 ish days) before potentially overwriting
 the same timestamp when reporting data.

 Basically a timestamp (rounded to the minute) can be counted and reported for about 2 weeks
 each time it reports it will use a slightly different timestamp so as to not overwrite
 previous counts
 */
@ToString
@EqualsAndHashCode
public class TimestampCounter extends Cloneable implements TimeCollector
{
	private Map<String, String> m_reportContext = new HashMap<>();
	private volatile Map<Instant, AtomicLong> m_timeBuckets = new HashMap<>();
	private Object m_mapLock = new Object();

	private final Clock m_clock;

	@Setter
	protected long incrementFrequency = 20_000L; //Increments the reported timestamp every 20 sec

	@Setter
	protected long bucketSize = 60_000L;  //Aggregates report time to 1 minute intervals, this should match your reporting interval (defaults to 1 min)

	public TimestampCounter()
	{
		this(new Clock());
	}

	public TimestampCounter(Clock clock)
	{
		m_clock = clock;
	}


	@Override
	public void put(Instant value)
	{
		Instant bucketTime = value.truncatedTo(ChronoUnit.MINUTES);

		synchronized (m_mapLock)
		{
			AtomicLong counter = m_timeBuckets.computeIfAbsent(bucketTime, (key) -> new AtomicLong());

			counter.incrementAndGet();
		}
	}

	@Override
	public void put(Instant time, Instant value)
	{
		put(value);
	}

	@Override
	public Collector clone()
	{
		TimestampCounter clone = (TimestampCounter) super.clone();
		clone.m_timeBuckets = new HashMap<>();
		clone.m_mapLock = new Object();
		clone.m_reportContext = m_reportContext;

		return clone;
	}

	@Override
	public void init(MetricsContext context)
	{
		Map<String, String> reportContext = new HashMap<>();
		reportContext.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
		reportContext.put(TYPE_KEY, TYPE_COUNTER_VALUE);

		m_reportContext = Collections.unmodifiableMap(reportContext);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		metricReporter.setContext(m_reportContext);
		Map<Instant, AtomicLong> timeBuckets;
		synchronized (m_mapLock)
		{
			timeBuckets = m_timeBuckets;
			m_timeBuckets = new ConcurrentHashMap<>();
		}

		long nowMillis = m_clock.now();

		long additionalMillis = (nowMillis / incrementFrequency) % bucketSize;

		for (Instant instant : timeBuckets.keySet())
		{
			AtomicLong counter = timeBuckets.get(instant);

			metricReporter.put("count", new LongValue(counter.get())).setTime(instant.plusMillis(additionalMillis));
		}

	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
