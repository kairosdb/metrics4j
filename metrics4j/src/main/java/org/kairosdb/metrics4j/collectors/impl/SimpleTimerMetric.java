package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.internal.DoubleTimeReporter;
import org.kairosdb.metrics4j.internal.LongTimeReporter;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.kairosdb.metrics4j.collectors.impl.SimpleStats.AVG_QUANTILE;
import static org.kairosdb.metrics4j.collectors.impl.SimpleStats.MAX_QUANTILE;
import static org.kairosdb.metrics4j.collectors.impl.SimpleStats.MIN_QUANTILE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_SUMMARY_VALUE;
import static org.kairosdb.metrics4j.reporting.SummaryContext.COUNT_CONTEXT;
import static org.kairosdb.metrics4j.reporting.SummaryContext.SUM_CONTEXT;

@ToString
@EqualsAndHashCode
public class SimpleTimerMetric extends TimerCollector implements DurationCollector, MetricCollector
{
	private DoubleTimeReporter m_doubleTimeReporter = new DoubleTimeReporter(ChronoUnit.MILLIS);
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
		super();
		clear();
	}

	public SimpleTimerMetric(ChronoUnit unit, boolean reportZero)
	{
		this();
		setReportUnit(unit);
		this.reportZero = reportZero;
	}

	@Override
	public void setReportUnit(ChronoUnit reportUnit)
	{
		super.setReportUnit(reportUnit);
		//We have to maintain our own double reporter to get averages
		m_doubleTimeReporter = new DoubleTimeReporter(reportUnit);
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
			metricReporter.setContext(m_reportContext);
			metricReporter.put("min", m_timeReporter.getValue(data.min)).setSampleContext(MIN_QUANTILE);
			metricReporter.put("max", m_timeReporter.getValue(data.max)).setSampleContext(MAX_QUANTILE);
			metricReporter.put("total", m_timeReporter.getValue(data.sum)).setSampleContext(SUM_CONTEXT);
			metricReporter.put("count", new LongValue(data.count)).setSampleContext(COUNT_CONTEXT);
			metricReporter.put("avg", m_doubleTimeReporter.getValue(data.avg)).setSampleContext(AVG_QUANTILE);
		}
		else if (reportZero)
		{
			metricReporter.setContext(m_reportContext);
			metricReporter.put("min", new LongValue(0L)).setSampleContext(MIN_QUANTILE);
			metricReporter.put("max", new LongValue(0L)).setSampleContext(MAX_QUANTILE);
			metricReporter.put("total", new LongValue(0L)).setSampleContext(SUM_CONTEXT);
			metricReporter.put("count", new LongValue(0L)).setSampleContext(COUNT_CONTEXT);
			metricReporter.put("avg", new DoubleValue(0.0)).setSampleContext(AVG_QUANTILE);
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
		ret.setReportUnit(getReportUnit());
		ret.setReportFormat(getReportFormat());
		ret.reportZero = reportZero;
		return ret;
	}

	@Override
	public void init(MetricsContext context)
	{
		m_reportContext.put(TYPE_KEY, TYPE_SUMMARY_VALUE);
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

