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
import org.kairosdb.metrics4j.reporting.SummaryContext;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_CUMULATIVE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_SUMMARY_VALUE;
import static org.kairosdb.metrics4j.reporting.SummaryContext.COUNT_CONTEXT;
import static org.kairosdb.metrics4j.reporting.SummaryContext.SUM_CONTEXT;
import static org.kairosdb.metrics4j.reporting.SummaryContext.createQuantile;

@ToString
@EqualsAndHashCode
public class SimpleStats implements LongCollector, MetricCollector
{
	public static final SummaryContext MIN_QUANTILE = createQuantile(0.0);
	public static final SummaryContext MAX_QUANTILE = createQuantile(1.0);
	public static final SummaryContext AVG_QUANTILE = createQuantile(0.5);
	public static final LongValue ZERO_LONG = new LongValue(0L);
	public static final DoubleValue ZERO_DOUBLE = new DoubleValue(0.0);

	protected long m_min;
	protected long m_max;
	protected long m_sum;
	protected long m_count;
	@EqualsAndHashCode.Exclude
	protected final Object m_dataLock = new Object();

	/**
	 Report zero values during interval if no data is received.
	 */
	@Setter
	protected boolean reportZero = false;
	private Map<String, String> m_reportContext = new HashMap<>();

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
		Map<String, String> reportContext = new HashMap<>();

		reportContext.put(TYPE_KEY, TYPE_SUMMARY_VALUE);

		m_reportContext = Collections.unmodifiableMap(reportContext);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		synchronized (m_dataLock)
		{
			if (m_count != 0)
			{
				metricReporter.setContext(m_reportContext);
				Map<String, MetricValue> values = new HashMap<>();
				metricReporter.put("min", new LongValue(m_min)).setSampleContext(MIN_QUANTILE);
				metricReporter.put("max", new LongValue(m_max)).setSampleContext(MAX_QUANTILE);
				metricReporter.put("sum", new LongValue(m_sum)).setSampleContext(SUM_CONTEXT);
				metricReporter.put("count", new LongValue(m_count)).setSampleContext(COUNT_CONTEXT);
				metricReporter.put("avg", new DoubleValue(((double)m_sum)/((double)m_count))).setSampleContext(AVG_QUANTILE);
			}
			else if (reportZero)
			{
				metricReporter.setContext(m_reportContext);
				metricReporter.put("min", ZERO_LONG).setSampleContext(MIN_QUANTILE);
				metricReporter.put("max", ZERO_LONG).setSampleContext(MAX_QUANTILE);
				metricReporter.put("sum", ZERO_LONG).setSampleContext(SUM_CONTEXT);
				metricReporter.put("count", ZERO_LONG).setSampleContext(COUNT_CONTEXT);
				metricReporter.put("avg", ZERO_DOUBLE).setSampleContext(AVG_QUANTILE);
			}

			reset();
		}
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
