package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.helpers.ChainedCollector;
import org.kairosdb.metrics4j.collectors.impl.*;
import org.kairosdb.metrics4j.configuration.ConfigurationException;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CollectorsTest
{

	public void initCloneCheck(ChainedCollector chainedCollector)
	{
		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		chainedCollector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne");
		prefixes.add("prefixTwo");
		chainedCollector.setPrefixes(prefixes);

		Collector clone = chainedCollector.clone();

		assertThat(clone).isEqualToComparingFieldByField(chainedCollector);
	}

	@Test
	public void testChainedDoubleCollector_clone()
	{
		initCloneCheck(new ChainedDoubleCollector());
	}

	@Test
	public void testChainedLongCollector_clone()
	{
		initCloneCheck(new ChainedLongCollector());
	}

	@Test
	public void testChainedDurationCollector_clone()
	{
		initCloneCheck(new ChainedDurationCollector());
	}

	@Test
	public void testChainedStringCollector_clone()
	{
		initCloneCheck(new ChainedStringCollector());
	}

	@Test
	public void testChainedTimeCollector_clone()
	{
		initCloneCheck(new ChainedTimeCollector());
	}

	@Test
	public void testChainedDoubleCollector_validate()
	{
		ChainedCollector chainedCollector = new ChainedDoubleCollector();

		chainedCollector.validateCollector(new DoubleGauge());

		assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> chainedCollector.validateCollector(new LongGauge()));
	}

	@Test
	public void testChainedLongCollector_validate()
	{
		ChainedCollector chainedCollector = new ChainedLongCollector();

		chainedCollector.validateCollector(new LongGauge());

		assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> chainedCollector.validateCollector(new DoubleGauge()));
	}

	@Test
	public void testChainedStringCollector_validate()
	{
		ChainedCollector chainedCollector = new ChainedStringCollector();

		chainedCollector.validateCollector(new StringReporter());

		assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> chainedCollector.validateCollector(new LongGauge()));
	}

	@Test
	public void testChainedDurationCollector_validate()
	{
		ChainedCollector chainedCollector = new ChainedDurationCollector();

		chainedCollector.validateCollector(new SimpleTimerMetric());

		assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> chainedCollector.validateCollector(new LongGauge()));
	}

	@Test
	public void testChainedTimeCollector_validate()
	{
		ChainedCollector chainedCollector = new ChainedTimeCollector();

		chainedCollector.validateCollector(new TimeDelta());

		assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> chainedCollector.validateCollector(new LongGauge()));
	}

	@Test
	public void testChainedCollectorInit_arraySizeCheck()
	{
		ChainedCollector collector = new ChainedLongCollector();

		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		collector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne");
		collector.setPrefixes(prefixes);

		MetricsContext context = mock(MetricsContext.class);

		assertThatExceptionOfType(ConfigurationException.class).isThrownBy(() -> collector.init(context));
	}

	@Test
	public void testChainedLongCollector_initAndPut()
	{
		ChainedLongCollector collector = new ChainedLongCollector();

		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		collector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne");
		prefixes.add("prefixTwo");
		collector.setPrefixes(prefixes);

		MetricsContext context = mock(MetricsContext.class);
		LongCollector collector1 = spy(new LongCounter());
		LongCollector collector2 = spy(new LongCounter());

		//make sure the clone doesn't actually clone or we loose the spy
		when(collector1.clone()).thenReturn(collector1);
		when(collector2.clone()).thenReturn(collector2);

		when(context.getCollector("collectorOne")).thenReturn(collector1);
		when(context.getCollector("collectorTwo")).thenReturn(collector2);

		collector.init(context);

		collector.put(42);

		verify(collector1).put(42);
		verify(collector2).put(42);
	}

	@Test
	public void testChainedDoubleCollector_initAndPut()
	{
		ChainedDoubleCollector collector = new ChainedDoubleCollector();

		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		collector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne");
		prefixes.add("prefixTwo");
		collector.setPrefixes(prefixes);

		MetricsContext context = mock(MetricsContext.class);
		DoubleCollector collector1 = spy(new DoubleCounter());
		DoubleCollector collector2 = spy(new DoubleCounter());

		//make sure the clone doesn't actually clone or we loose the spy
		when(collector1.clone()).thenReturn(collector1);
		when(collector2.clone()).thenReturn(collector2);

		when(context.getCollector("collectorOne")).thenReturn(collector1);
		when(context.getCollector("collectorTwo")).thenReturn(collector2);

		collector.init(context);

		collector.put(42.0);

		verify(collector1).put(42.0);
		verify(collector2).put(42.0);
	}

	@Test
	public void testChainedStringCollector_initAndPut()
	{
		ChainedStringCollector collector = new ChainedStringCollector();

		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		collector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne");
		prefixes.add("prefixTwo");
		collector.setPrefixes(prefixes);

		MetricsContext context = mock(MetricsContext.class);
		StringCollector collector1 = spy(new StringReporter());
		StringCollector collector2 = spy(new StringReporter());

		//make sure the clone doesn't actually clone or we loose the spy
		when(collector1.clone()).thenReturn(collector1);
		when(collector2.clone()).thenReturn(collector2);

		when(context.getCollector("collectorOne")).thenReturn(collector1);
		when(context.getCollector("collectorTwo")).thenReturn(collector2);

		collector.init(context);

		collector.put("42");

		verify(collector1).put("42");
		verify(collector2).put("42");
	}

	@Test
	public void testChainedDurationCollector_initAndPut()
	{
		ChainedDurationCollector collector = new ChainedDurationCollector();

		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		collector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne");
		prefixes.add("prefixTwo");
		collector.setPrefixes(prefixes);

		MetricsContext context = mock(MetricsContext.class);
		DurationCollector collector1 = spy(new SimpleTimerMetric());
		DurationCollector collector2 = spy(new SimpleTimerMetric());

		//make sure the clone doesn't actually clone or we loose the spy
		when(collector1.clone()).thenReturn(collector1);
		when(collector2.clone()).thenReturn(collector2);

		when(context.getCollector("collectorOne")).thenReturn(collector1);
		when(context.getCollector("collectorTwo")).thenReturn(collector2);

		collector.init(context);

		collector.put(Duration.ofHours(42));

		verify(collector1).put(Duration.ofHours(42));
		verify(collector2).put(Duration.ofHours(42));
	}

	@Test
	public void testChainedTimeCollector_initAndPut()
	{
		ChainedTimeCollector collector = new ChainedTimeCollector();

		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		collector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne");
		prefixes.add("prefixTwo");
		collector.setPrefixes(prefixes);

		MetricsContext context = mock(MetricsContext.class);
		TimeCollector collector1 = spy(new TimeDelta());
		TimeCollector collector2 = spy(new TimeDelta());

		//make sure the clone doesn't actually clone or we loose the spy
		when(collector1.clone()).thenReturn(collector1);
		when(collector2.clone()).thenReturn(collector2);

		when(context.getCollector("collectorOne")).thenReturn(collector1);
		when(context.getCollector("collectorTwo")).thenReturn(collector2);

		collector.init(context);

		verify(collector1).init(context);
		verify(collector2).init(context);

		Instant now = Instant.now();
		collector.put(now);

		verify(collector1).put(now);
		verify(collector2).put(now);
	}

	@Test
	public void testChainedCollector_initAndClone_onSubCollectors()
	{
		ChainedLongCollector collector = new ChainedLongCollector();

		List<String> collectors = new ArrayList<>();
		collectors.add("collectorOne");
		collectors.add("collectorTwo");
		collector.setCollectors(collectors);

		List<String> prefixes = new ArrayList<>();
		prefixes.add("prefixOne.");
		prefixes.add("prefixTwo.");
		collector.setPrefixes(prefixes);

		MetricsContext context = mock(MetricsContext.class);

		when(context.getCollector("collectorOne")).thenReturn(new LongCounter());
		when(context.getCollector("collectorTwo")).thenReturn(new LongCounter());

		collector.init(context);

		ChainedLongCollector clone = (ChainedLongCollector)collector.clone();

		collector.put(42);

		//The clone should not interact with the others collectors
		clone.put(5);

		MetricReporter reporter = mock(MetricReporter.class);
		collector.reportMetric(reporter);

		verify(reporter).put(eq("prefixOne.count"), eq(new LongValue(42)));
		verify(reporter).put(eq("prefixTwo.count"), eq(new LongValue(42)));

		MetricReporter cloneReporter = mock(MetricReporter.class);
		clone.reportMetric(cloneReporter);

		verify(cloneReporter).put(eq("prefixOne.count"), eq(new LongValue(5)));
		verify(cloneReporter).put(eq("prefixTwo.count"), eq(new LongValue(5)));
	}
}
