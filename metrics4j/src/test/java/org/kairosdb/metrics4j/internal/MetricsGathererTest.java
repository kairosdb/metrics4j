package org.kairosdb.metrics4j.internal;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MetricsGathererTest
{
	private class TestMetricsGatherer extends MetricsGatherer
	{
		private Map<TagKey, AgedMetricCollector> m_collectors = new HashMap<>();

		public void addCollector(TagKey tagKey, AgedMetricCollector collector)
		{
			m_collectors.put(tagKey, collector);
		}

		@Override
		public MetricCollector getCollector(TagKey tagKey)
		{
			return null;
		}

		@Override
		protected ArgKey getArgKey()
		{
			return new ArgKey()
			{
				@Override
				public List<String> getConfigPath()
				{
					return Collections.emptyList();
				}

				@Override
				public String getMethodName()
				{
					return "testMethod";
				}

				@Override
				public String getClassName()
				{
					return "org.TestClass";
				}
			};
		}

		@Override
		protected Map<TagKey, AgedMetricCollector> getCollectors()
		{
			return m_collectors;
		}
	}

	private class TestCollector implements MetricCollector
	{
		private final boolean m_report;

		public TestCollector(boolean report)
		{
			m_report = report;
		}

		@Override
		public void reportMetric(MetricReporter metricReporter)
		{
			if (m_report)
				metricReporter.put("myField", new LongValue(42));
		}

		@Override
		public void setContextProperties(Map<String, String> contextProperties)
		{

		}
	}

	private class FixedMetricCollector extends MetricsGatherer.AgedMetricCollector
	{
		private Duration m_age;

		public FixedMetricCollector(Duration age, MetricCollector metricCollector)
		{
			super(metricCollector);
			m_age = age;
		}

		@Override
		public Duration getAge()
		{
			return m_age;
		}
	}


	@Test
	public void testAgedOutCollector()
	{
		TestMetricsGatherer tmg = new TestMetricsGatherer();

		MetricsGatherer.AgedMetricCollector metricCollector = mock(MetricsGatherer.AgedMetricCollector.class);
		when(metricCollector.getAge()).thenReturn(Duration.ofMinutes(11));
		when(metricCollector.getMetricCollector()).thenReturn(new TestCollector(false));

		tmg.addCollector(TagKey.newBuilder().addTag("tag1", "value1").build(),
				metricCollector);

		tmg.gatherMetrics(Instant.now());

		assertThat(tmg.getCollectors()).isEmpty();
		verify(metricCollector, never()).updateLastUsed();
	}

	@Test
	public void testAgedOutCollector_reporting_but_aged()
	{
		TestMetricsGatherer tmg = new TestMetricsGatherer();
		MetricsGatherer.AgedMetricCollector metricCollector = mock(MetricsGatherer.AgedMetricCollector.class);
		when(metricCollector.getAge()).thenReturn(Duration.ofMinutes(11));
		when(metricCollector.getMetricCollector()).thenReturn(new TestCollector(true));

		tmg.addCollector(TagKey.newBuilder().addTag("tag1", "value1").build(),
				metricCollector);

		tmg.gatherMetrics(Instant.now());

		assertThat(tmg.getCollectors().size()).isEqualTo(1);
		verify(metricCollector).updateLastUsed();
	}

	@Test
	public void testAgedOutCollector_not_reporting_not_aged()
	{
		TestMetricsGatherer tmg = new TestMetricsGatherer();
		MetricsGatherer.AgedMetricCollector metricCollector = mock(MetricsGatherer.AgedMetricCollector.class);
		when(metricCollector.getAge()).thenReturn(Duration.ofMinutes(1));
		when(metricCollector.getMetricCollector()).thenReturn(new TestCollector(false));

		tmg.addCollector(TagKey.newBuilder().addTag("tag1", "value1").build(),
				metricCollector);

		tmg.gatherMetrics(Instant.now());

		assertThat(tmg.getCollectors().size()).isEqualTo(1);
		verify(metricCollector, never()).updateLastUsed();
	}
}
