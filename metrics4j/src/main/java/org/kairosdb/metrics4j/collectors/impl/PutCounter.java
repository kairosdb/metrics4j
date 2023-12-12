package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.collectors.TimeCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_CUMULATIVE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;

/**
 Counts the number of times put is called to pass on a metric.
 */
public class PutCounter extends TimerCollector implements LongCollector, DoubleCollector,
		StringCollector, DurationCollector, TimeCollector, MetricCollector
{
	private Map<String, String> m_reportContext = new HashMap<>();
	@EqualsAndHashCode.Exclude
	protected final AtomicLong m_count = new AtomicLong(0);

	@Setter
	protected boolean reset = false;

	@Setter
	protected boolean reportZero = true;

	public PutCounter(boolean reset, boolean reportZero)
	{
		super();
		this.reset = reset;
		this.reportZero = reportZero;
	}

	public PutCounter()
	{
		this(false, true);
	}

	@Override
	public void put(long count)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Instant time, long value)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;
		metricReporter.setContext(m_reportContext);

		if (reset)
			value = m_count.getAndSet(0);
		else
			value = m_count.longValue();

		if (value != 0L || reportZero)
			metricReporter.put("count", new LongValue(value));
	}

	@Override
	public void init(MetricsContext context)
	{
		Map<String, String> reportContext = new HashMap<>();
		if (reset)
			reportContext.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
		else
			reportContext.put(AGGREGATION_KEY, AGGREGATION_CUMULATIVE_VALUE);

		reportContext.put(TYPE_KEY, TYPE_COUNTER_VALUE);

		m_reportContext = Collections.unmodifiableMap(reportContext);
	}

	@Override
	public Collector clone()
	{
		PutCounter ret = new PutCounter(reset, reportZero);
		ret.m_reportContext = m_reportContext;
		return ret;
	}

	@Override
	public void put(double value)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Instant time, double value)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Duration duration)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Instant time, Duration duration)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(String value)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Instant time, String value)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Instant time)
	{
		m_count.incrementAndGet();
	}

	@Override
	public void put(Instant time, Instant value)
	{
		m_count.incrementAndGet();
	}
}
