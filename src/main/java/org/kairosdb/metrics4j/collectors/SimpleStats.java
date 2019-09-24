package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;

import java.util.HashMap;
import java.util.Map;

public class SimpleStats implements LongCollector, MetricCollector
{
	private long m_min;
	private long m_max;
	private long m_sum;
	private long m_count;
	private final Object m_dataLock = new Object();
	private static final Map<String, MetricValue> s_emptyValues = new HashMap<>();

	{
		s_emptyValues.put("min", new LongValue(0L));
		s_emptyValues.put("max", new LongValue(0L));
		s_emptyValues.put("sum", new LongValue(0L));
		s_emptyValues.put("count", new LongValue(0L));
		s_emptyValues.put("avg", new DoubleValue(0.0));
	}

	public SimpleStats()
	{
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
		return new SimpleStats();
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
			else
			{
				metricReporter.put("min", new LongValue(0L));
				metricReporter.put("max", new LongValue(0L));
				metricReporter.put("sum", new LongValue(0L));
				metricReporter.put("count", new LongValue(0L));
				metricReporter.put("avg", new DoubleValue(0.0));
			}

			reset();
		}
	}
}
