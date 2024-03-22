package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_CUMULATIVE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;

@ToString
@EqualsAndHashCode
public class DoubleCounter implements DoubleCollector
{
	private Map<String, String> m_reportContext = new HashMap<>();
	protected double m_count;

	@EqualsAndHashCode.Exclude
	protected Object m_counterLock = new Object();

	@Setter
	protected boolean reset;

	@Setter
	protected boolean reportZero;

	public DoubleCounter(boolean reset, boolean reportZero)
	{
		this.reset = reset;
		this.reportZero = reportZero;
	}

	public DoubleCounter()
	{
		this(false, true);
	}

	@Override
	public void put(double value)
	{
		synchronized (m_counterLock)
		{
			m_count += value;
		}
	}

	@Override
	public void put(Instant time, double value)
	{
		put(value);
	}

	@Override
	public Collector clone()
	{
		DoubleCounter ret = new DoubleCounter(reset, reportZero);
		ret.m_reportContext = m_reportContext;
		return ret;
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
	public void reportMetric(MetricReporter metricReporter)
	{
		synchronized (m_counterLock)
		{
			if (m_count != 0.0 || reportZero)
			{
				metricReporter.setContext(m_reportContext);
				metricReporter.put("count", new DoubleValue(m_count));
			}

			if (reset)
				m_count = 0.0;
		}
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
