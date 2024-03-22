package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_GAUGE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;

@ToString
@EqualsAndHashCode
public class LastTime extends TimerCollector implements DurationCollector, MetricCollector
{
	protected AtomicReference<Duration> m_lastTime = new AtomicReference<>();

	@Override
	public Collector clone()
	{
		LastTime ret = new LastTime();
		ret.setReportUnit(getReportUnit());
		ret.setReportFormat(getReportFormat());

		return ret;
	}

	@Override
	public void init(MetricsContext context)
	{
		m_reportContext.put(TYPE_KEY, TYPE_GAUGE_VALUE);
		//m_reportContext.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		metricReporter.setContext(m_reportContext);
		Duration lastTime = m_lastTime.getAndSet(null);

		if (lastTime != null)
		{
			metricReporter.put("value", m_timeReporter.getValue(lastTime));
		}

	}

	@Override
	public void put(Duration duration)
	{
		m_lastTime.set(duration);
	}

	@Override
	public void put(Instant time, Duration duration)
	{
		put(duration);
	}


}
