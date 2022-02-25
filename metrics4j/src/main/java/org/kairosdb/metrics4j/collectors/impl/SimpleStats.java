package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class SimpleStats implements LongCollector, MetricCollector
{
	protected long m_min;
	protected long m_max;
	protected long m_sum;
	protected long m_count;
	@EqualsAndHashCode.Exclude
	protected final Object m_dataLock = new Object();
	protected static final Map<String, MetricValue> s_emptyValues = new HashMap<>();

	/**
	 Report zero values during interval if no data is received.
	 */
	@Setter
	protected boolean reportZero = false;

	{
		s_emptyValues.put("min", new LongValue(0L));
		s_emptyValues.put("max", new LongValue(0L));
		s_emptyValues.put("sum", new LongValue(0L));
		s_emptyValues.put("count", new LongValue(0L));
		s_emptyValues.put("avg", new DoubleValue(0.0));
	}

	public SimpleStats()
	{
		this(false);
	}

	public SimpleStats(boolean reportZero)
	{
		this.reportZero = reportZero;
		reset();
	}

	@Override
	public void put(long value)
	{
		synchronized (m_dataLock)
		{
			m_min = Math.min(m_min, value);
			m_max = Math.max(m_max, value);
			m_sum += value;
			m_count++;
		}
	}

	@Override
	public void put(Instant time, long value)
	{
		put(value);
	}

	public void reset()
	{
		m_min = Long.MAX_VALUE;
		m_max = Long.MIN_VALUE;
		m_sum = 0;
		m_count = 0;
	}

	@Override
	public Collector clone()
	{
		return new SimpleStats(reportZero);
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		synchronized (m_dataLock)
		{
			if (m_count != 0)
			{
				Map<String, MetricValue> values = new HashMap<>();
				metricReporter.put("min", new LongValue(m_min));
				metricReporter.put("max", new LongValue(m_max));
				metricReporter.put("sum", new LongValue(m_sum));
				metricReporter.put("count", new LongValue(m_count));
				metricReporter.put("avg", new DoubleValue(((double)m_sum)/((double)m_count)));
			}
			else if (reportZero)
			{
				for (Map.Entry<String, MetricValue> emptyValue : s_emptyValues.entrySet())
				{
					metricReporter.put(emptyValue.getKey(), emptyValue.getValue());
				}
			}

			reset();
		}
	}
}
