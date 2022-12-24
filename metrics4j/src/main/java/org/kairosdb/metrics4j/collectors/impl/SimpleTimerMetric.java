package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ToString
@EqualsAndHashCode
public class SimpleTimerMetric extends TimerCollector implements DurationCollector, MetricCollector
{
	protected Duration m_min;
	protected Duration m_max;
	protected Duration m_sum;
	protected long m_count;

	@EqualsAndHashCode.Exclude
	protected final Object m_dataLock = new Object();


	/**
	 Report zero values during interval if no data is received.
	 */
	@Setter
	protected boolean reportZero = false;

	public SimpleTimerMetric()
	{
		clear();
	}

	public SimpleTimerMetric(ChronoUnit unit, boolean reportZero)
	{
		this();
		this.reportUnit = unit;
		this.reportZero = reportZero;
	}


	private void clear()
	{
		m_min = Duration.of(Long.MAX_VALUE, ChronoUnit.MILLIS);
		m_max = Duration.ofMillis(0);
		m_sum = Duration.ofMillis(0);
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
				ret = new Data(m_min, m_max, m_sum, m_count, m_sum.dividedBy(m_count));
			else
				ret = new Data(Duration.ZERO, Duration.ZERO, Duration.ZERO, 0, Duration.ZERO);

			clear();
			return ret;
		}
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		Data data = getAndClear();

		if (data.count != 0)
		{
			long total = getValue(reportUnit, data.sum);
			metricReporter.put("min", new LongValue(getValue(reportUnit, data.min)));
			metricReporter.put("max", new LongValue(getValue(reportUnit, data.max)));
			metricReporter.put("total", new LongValue(total));
			metricReporter.put("count", new LongValue(data.count));
			metricReporter.put("avg", new DoubleValue((double)total / (double)data.count));
		}
		else if (reportZero)
		{
			metricReporter.put("min", new LongValue(0L));
			metricReporter.put("max", new LongValue(0L));
			metricReporter.put("total", new LongValue(0L));
			metricReporter.put("count", new LongValue(0L));
			metricReporter.put("avg", new DoubleValue(0.0));
		}
	}

	@Override
	public void put(Duration duration)
	{
		synchronized (m_dataLock)
		{
			m_min = m_min.compareTo(duration) < 0 ? m_min : duration;
			m_max = m_max.compareTo(duration) > 0 ? m_max : duration;
			m_sum = m_sum.plus(duration);
			m_count++;
		}
	}

	@Override
	public void put(Instant time, Duration duration)
	{
		put(duration);
	}


	@Override
	public Collector clone()
	{
		SimpleTimerMetric ret = new SimpleTimerMetric();
		ret.reportUnit = reportUnit;
		ret.reportZero = reportZero;
		return ret;
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	public static class Data
	{
		public final Duration min;
		public final Duration max;
		public final Duration sum;
		public final long count;
		public final Duration avg;

		public Data(Duration min, Duration max, Duration sum, long count, Duration avg)
		{
			this.min = min;
			this.max = max;
			this.sum = sum;
			this.count = count;
			this.avg = avg;
		}
	}
}

