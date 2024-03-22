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
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_GAUGE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;

@ToString
@EqualsAndHashCode
public class LongGauge implements LongCollector
{
	protected Map<String, String> m_reportContext = new HashMap<>();
	@EqualsAndHashCode.Exclude
	protected final AtomicLong m_gauge = new AtomicLong(0);

	@Setter
	protected boolean reset = false;

	public LongGauge(boolean reset)
	{
		super();
		this.reset = reset;
	}

	public LongGauge()
	{
		this(false);
	}


	@Override
	public void put(long value)
	{
		m_gauge.set(value);
	}

	@Override
	public void put(Instant time, long count)
	{
		put(count);
	}

	@Override
	public Collector clone()
	{
		LongGauge ret = new LongGauge(reset);
		ret.m_reportContext = m_reportContext;
		return ret;
	}

	@Override
	public void init(MetricsContext context)
	{
		Map<String, String> reportContext = new HashMap<>();

		reportContext.put(TYPE_KEY, TYPE_GAUGE_VALUE);

		m_reportContext = Collections.unmodifiableMap(reportContext);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;
		metricReporter.setContext(m_reportContext);

		if (reset)
			value = m_gauge.getAndSet(0);
		else
			value = m_gauge.get();

		metricReporter.put("gauge", new LongValue(value));
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}

}
