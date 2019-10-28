package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.StringValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StringReporter implements StringCollector
{
	private List<Instant> m_times = new ArrayList<>();
	private List<String> m_strings = new ArrayList<>();
	private Object m_stringsLock = new Object();

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
	public Collector clone()
	{
		return null;
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
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
			metricReporter.put("value", new StringValue(data.get(i)), times.get(i));
		}
	}
}
