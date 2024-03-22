package org.kairosdb.metrics4j.collectors.impl;

import org.kairosdb.metrics4j.MetricThreadHelper;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.*;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_CUMULATIVE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;

/**
 This collector does not do any aggregation.  Whatever value was put into the collector
 is reported using the time of the put or the Instant if one was provided.  This collector
 will also honor any request time set on the thread via MetricThreadHelper
 */
public class BagCollector extends TimerCollector implements LongCollector, DoubleCollector, StringCollector
{
	private Map<String, String> m_reportContext = new HashMap<>();
	private final Object m_bagLock = new Object();
	private volatile ArrayList<TimedValue> m_bag = new ArrayList<>();

	private void addToBag(Instant time, MetricValue value)
	{
		Instant reportTime = MetricThreadHelper.getReportTime();
		if (reportTime == Instant.MIN) //time was not set on thread.
			reportTime = time;

		synchronized (m_bagLock)
		{
			m_bag.add(new TimedValue(reportTime, value));
		}
	}

	@Override
	public void init(MetricsContext context)
	{
		Map<String, String> reportContext = new HashMap<>();
		reportContext.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
		reportContext.put(TYPE_KEY, TYPE_COUNTER_VALUE);

		m_reportContext = Collections.unmodifiableMap(reportContext);
	}

	@Override
	public Collector clone()
	{
		BagCollector ret = new BagCollector();
		ret.setReportUnit(getReportUnit());
		ret.setReportFormat(getReportFormat());
		ret.m_reportContext = m_reportContext;

		return ret;
	}

	@Override
	public void put(double value)
	{
		put(Instant.now(), value);
	}

	@Override
	public void put(Instant time, double value)
	{
		addToBag(time, new DoubleValue(value));
	}

	@Override
	public void put(Duration duration)
	{
		put(Instant.now(), duration);
	}

	@Override
	public void put(Instant time, Duration duration)
	{
		addToBag(time, m_timeReporter.getValue(duration));
	}

	@Override
	public void put(long value)
	{
		put(Instant.now(), value);
	}

	@Override
	public void put(Instant time, long value)
	{
		addToBag(time, new LongValue(value));
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		metricReporter.setContext(m_reportContext);
		ArrayList<TimedValue> oldBag;
		synchronized (m_bagLock)
		{
			oldBag = m_bag;
			m_bag = new ArrayList<>();
		}

		for (TimedValue timedValue : oldBag)
		{
			metricReporter.put("value", timedValue.m_value).setTime(timedValue.m_time);
		}
	}

	@Override
	public void put(String value)
	{
		put(Instant.now(), value);
	}

	@Override
	public void put(Instant time, String value)
	{
		m_bag.add(new TimedValue(time, new StringValue(value)));
	}

	private class TimedValue
	{
		public Instant m_time;
		public MetricValue m_value;

		public TimedValue(Instant time, MetricValue value)
		{
			m_time = time;
			m_value = value;
		}
	}
}
