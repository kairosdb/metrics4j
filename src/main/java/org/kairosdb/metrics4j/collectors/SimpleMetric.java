package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.time.Duration;
import java.util.HashMap;

public class SimpleMetric implements DurationCollector, ReportableMetric
{
	private long m_min;
	private long m_max;
	private long m_sum;
	private long m_count;
	private final Object m_dataLock = new Object();

	public SimpleMetric()
	{
		clear();
	}

	public void addValue(long value)
	{
		synchronized (m_dataLock)
		{
			m_min = Math.min(m_min, value);
			m_max = Math.max(m_max, value);
			m_sum += value;
			m_count++;
		}
	}

	private void clear()
	{
		m_min = Long.MAX_VALUE;
		m_max = Long.MIN_VALUE;
		m_sum = 0;
		m_count = 0;
	}

	/**
	 Not thread safe
	 @return
	 */
	public long getCount()
	{
		return m_count;
	}

	public Data getAndClear()
	{
		synchronized (m_dataLock)
		{
			Data ret;
			if (m_count != 0)
				ret = new Data(m_min, m_max, m_sum, m_count, ((double)m_sum)/((double)m_count));
			else
				ret = new Data(0, 0, 0, 0, 0.0);

			clear();
			return ret;
		}
	}

	@Override
	public void reportMetric(ReportedMetric reportedMetric)
	{
		Data data = getAndClear();
		HashMap<String, MetricValue> fields = new HashMap<>();
		fields.put("min", new DoubleValue(data.min));
		fields.put("max", new DoubleValue(data.max));
		fields.put("sum", new DoubleValue(data.sum));
		fields.put("count", new LongValue(data.count));
		fields.put("avg", new DoubleValue(data.avg));

		reportedMetric.setFields(fields);
	}

	@Override
	public void put(Duration duration)
	{

	}

	@Override
	public Collector clone()
	{
		return null;
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	public static class Data
	{
		public final long min;
		public final long max;
		public final long sum;
		public final long count;
		public final double avg;

		public Data(long min, long max, long sum, long count, double avg)
		{
			this.min = min;
			this.max = max;
			this.sum = sum;
			this.count = count;
			this.avg = avg;
		}
	}
}

