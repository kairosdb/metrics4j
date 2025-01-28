package org.kairosdb.metrics4j.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.collectors.impl.ChainedLongCollector;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.internal.LambdaArgKey;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.util.Collections;
import java.util.Map;
import java.util.function.LongSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ParseConfigTest
{
	private MetricConfig m_metricConfig;

	@BeforeEach
	public void setup()
	{
		m_metricConfig = MetricConfig.parseConfig("test_config.conf", "Not_there");
		MetricSourceManager.setMetricConfig(m_metricConfig);
	}


	@AfterEach
	public void tearDown()
	{
		MetricSourceManager.clearConfig();
	}


	@Test
	public void testChainCollector()
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);

		source.chainCount().put(5);

		ChainedLongCollector collector = (ChainedLongCollector) source.chainCount();

		MetricReporter reporter = mock(MetricReporter.class);

		collector.reportMetric(reporter);

		verify(reporter).put("count.count", new LongValue(5));
		verify(reporter).put("max.gauge", new LongValue(5));
	}

	@Test
	public void testSpaceInSourceName()
	{
		String className = "java.Memory.Garbage Collector";
		String methodName = "myMethod";

		MetricSourceManager.addSource(className, methodName, Map.of(), "help", () -> 42);

		LambdaArgKey argKey = new LambdaArgKey(className, methodName);
		assertThat(argKey.getConfigPath()).containsExactly("java", "Memory", "Garbage Collector", "myMethod");

		String metricNameForKey = m_metricConfig.getMetricNameForKey(argKey);

		assertThat(metricNameForKey).isEqualTo("simple-name");


	}
}
