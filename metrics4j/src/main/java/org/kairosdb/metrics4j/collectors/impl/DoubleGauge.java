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
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_GAUGE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;


@ToString
@EqualsAndHashCode
public class DoubleGauge implements DoubleCollector
{
	private Map<String, String> m_reportContext = new HashMap<>();
	protected double m_gauge = 0.0;

	@EqualsAndHashCode.Exclude
	protected Object m_counterLock = new Object();

	@Setter
	protected boolean reset;

	public DoubleGauge(boolean reset)
	{
		this.reset = reset;
	}

	public DoubleGauge()
	{
		this(false);
	}

	@Override
	public void put(double value)
	{
		synchronized (m_counterLock)
		{
			m_gauge = value;
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
		DoubleGauge ret = new DoubleGauge(reset);
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
		metricReporter.setContext(m_reportContext);
		synchronized (m_counterLock)
		{
			metricReporter.put("gauge", new DoubleValue(m_gauge));
			if (reset)
				m_gauge = 0.0;
		}
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
