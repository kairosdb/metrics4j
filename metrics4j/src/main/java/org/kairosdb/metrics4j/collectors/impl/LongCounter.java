package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

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


@ToString
@EqualsAndHashCode
public class LongCounter implements LongCollector
{
	private Map<String, String> m_reportContext = new HashMap<>();
	@EqualsAndHashCode.Exclude
	protected final AtomicLong m_count = new AtomicLong(0);

	@Setter
	protected boolean reset = false;

	@Setter
	protected boolean reportZero = true;

	public LongCounter(boolean reset, boolean reportZero)
	{
		super();
		this.reset = reset;
		this.reportZero = reportZero;
	}

	public LongCounter()
	{
		this(false, true);
	}

	@Override
	public void put(long count)
	{
		m_count.addAndGet(count);
	}

	@Override
	public void put(Instant time, long count)
	{
		put(count);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;

		if (reset)
			value = m_count.getAndSet(0);
		else
			value = m_count.longValue();

		if (value != 0L || reportZero)
		{
			metricReporter.setContext(m_reportContext);
			metricReporter.put("count", new LongValue(value));
		}
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

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
		LongCounter lc = new LongCounter(reset, reportZero);
		lc.m_reportContext = m_reportContext;
		return lc;
	}
}
