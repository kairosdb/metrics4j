package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.StringValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_STRING_VALUE;

@ToString
@EqualsAndHashCode
public class StringReporter implements StringCollector
{
	private Map<String, String> m_reportContext = new HashMap<>();
	protected List<Instant> m_times = new ArrayList<>();
	protected List<String> m_strings = new ArrayList<>();
	protected Object m_stringsLock = new Object();

	@Override
	public void put(String value)
	{
		synchronized (m_stringsLock)
		{
			m_times.add(Instant.now());
			m_strings.add(value);
		}
	}

	@Override
	public void put(Instant time, String value)
	{
		put(value);
	}

	@Override
	public Collector clone()
	{
		return new StringReporter();
	}

	@Override
	public void init(MetricsContext context)
	{
		Map<String, String> reportContext = new HashMap<>();
		reportContext.put(TYPE_KEY, TYPE_STRING_VALUE);
		m_reportContext = Collections.unmodifiableMap(reportContext);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		metricReporter.setContext(m_reportContext);
		List<String> data;
		List<Instant> times;
		synchronized (m_stringsLock)
		{
			data = m_strings;
			times = m_times;
			m_strings = new ArrayList<>();
			m_times = new ArrayList<>();
		}

		for (int i = 0; i < times.size(); i++)
		{
			metricReporter.put("value", new StringValue(data.get(i))).setTime(times.get(i));
		}
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
