package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@XmlRootElement(name = "collector")
public class SimpleTimerMetric extends TimerCollector implements DurationCollector, MetricCollector
{
	private Duration m_min;
	private Duration m_max;
	private Duration m_sum;
	private long m_count;
	private final Object m_dataLock = new Object();

	/**
	 Unit to report metric as.  Supported units are NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, DAYS
	 */
	@XmlAttribute(name = "reportUnit", required = false)
	private ChronoUnit m_reportUnit = ChronoUnit.MILLIS;

	/**
	 Report zero values during interval if no data is received.
	 */
	@XmlAttribute(name = "reportZero", required = false)
	private boolean m_reportZero = false;

	public SimpleTimerMetric()
	{
		clear();
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

	private long getValue(Duration duration)
	{
		switch (m_reportUnit)
		{
			case NANOS: return duration.toNanos();
			case MICROS: return duration.toNanos() / 1000;
			case MILLIS: return duration.toMillis();
			case SECONDS: return duration.getSeconds();
			case HOURS: return duration.toHours();
			case DAYS: return duration.toDays();
			default: return 0;
		}
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		Data data = getAndClear();

		if (data.count != 0)
		{
			long total = getValue(data.sum);
			metricReporter.put("min", new LongValue(getValue(data.min)));
			metricReporter.put("max", new LongValue(getValue(data.max)));
			metricReporter.put("total", new LongValue(total));
			metricReporter.put("count", new LongValue(data.count));
			metricReporter.put("avg", new DoubleValue((double)total / (double)data.count));
		}
		else if (m_reportZero)
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
	public Collector clone()
	{
		SimpleTimerMetric ret = new SimpleTimerMetric();
		ret.m_reportUnit = m_reportUnit;
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

