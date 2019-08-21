package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Duration;

@XmlRootElement(name = "collector")
public class SimpleTimerMetric extends TimerCollector implements DurationCollector, ReportableMetric
{
	private long m_min;
	private long m_max;
	private long m_sum;
	private long m_count;
	private final Object m_dataLock = new Object();

	public SimpleTimerMetric()
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

	private Data getAndClear()
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
	public void reportMetric(MetricReporter metricReporter)
	{
		Data data = getAndClear();
		metricReporter.put("min", new DoubleValue(data.min));
		metricReporter.put("max", new DoubleValue(data.max));
		metricReporter.put("sum", new DoubleValue(data.sum));
		metricReporter.put("count", new LongValue(data.count));
		metricReporter.put("avg", new DoubleValue(data.avg));
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

