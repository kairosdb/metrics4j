package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.configuration.TestFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSink implements MetricSink
{
	private Map<String, List<FormattedMetric.Sample>> m_results;

	public TestSink()
	{
		m_results = new HashMap<>();
	}

	public List<FormattedMetric.Sample> getResults(String method)
	{
		return m_results.get(method);
	}

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		System.out.println("Reporting metrics");
		for (FormattedMetric metric : metrics)
		{
			m_results.put(metric.getMethodName(), metric.getSamples());
			System.out.println(metric.getMethodName());
		}
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return new TestFormatter();
	}

	@Override
	public void init(MetricsContext context)
	{

	}
}
