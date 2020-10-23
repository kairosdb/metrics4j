package org.kairosdb.metrics4j.collectors.helpers;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.configuration.ConfigurationException;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.MetricValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
public abstract class ChainedCollector<C extends Collector> extends Cloneable implements Collector
{
	protected List<PrefixMetricReporter<C>> m_chainedCollectors = new ArrayList<>();

	@Setter
	protected List<String> collectors;

	@Setter
	protected List<String> prefixes;

	public abstract C validateCollector(Collector collector);

	public Collector clone()
	{
		ChainedCollector clone = (ChainedCollector)super.clone();
		List<PrefixMetricReporter<C>> clonedCollectors = new ArrayList<>();

		for (Object chainedCollector : clone.m_chainedCollectors)
		{
			clonedCollectors.add(((PrefixMetricReporter)chainedCollector).clone());
		}

		clone.m_chainedCollectors = clonedCollectors;

		return clone;
	}

	@Override
	public void init(MetricsContext context)
	{
		if (collectors.size() != prefixes.size())
			throw new ConfigurationException("The number of prefixes and collectors must match when using a chained collector");

		for (int I = 0; I < collectors.size(); I++)
		{
			Collector contextCollector = context.getCollector(collectors.get(I));
			String prefix = prefixes.get(I);

			if (contextCollector == null)
				throw new ConfigurationException("Collector name '"+collectors.get(I)+"' is invalid");

			C validatedCollector = validateCollector(contextCollector.clone());
			validatedCollector.init(context);

			m_chainedCollectors.add(new PrefixMetricReporter<C>(validatedCollector, prefix));
		}
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		for (PrefixMetricReporter<C> chainedCollector : m_chainedCollectors)
		{
			chainedCollector.reportMetric(metricReporter);
		}
	}

	protected static class PrefixMetricReporter<C extends Collector> implements MetricReporter
	{
		private MetricReporter m_innerReporter;
		private final C m_collector;
		private final String m_prefix;

		private PrefixMetricReporter(C collector, String prefix)
		{
			m_collector = collector;
			m_prefix = prefix;
		}

		@Override
		public PrefixMetricReporter<C> clone()
		{
			return new PrefixMetricReporter(m_collector.clone(), m_prefix);
		}

		public C getCollector()
		{
			return m_collector;
		}

		public void reportMetric(MetricReporter reporter)
		{
			m_innerReporter = reporter;
			m_collector.reportMetric(this);
		}

		@Override
		public void put(String field, MetricValue value)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(m_prefix).append(field);

			m_innerReporter.put(sb.toString(), value);
		}

		@Override
		public void put(String field, MetricValue value, Instant time)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(m_prefix).append(field);

			m_innerReporter.put(sb.toString(), value, time);
		}
	}
}
