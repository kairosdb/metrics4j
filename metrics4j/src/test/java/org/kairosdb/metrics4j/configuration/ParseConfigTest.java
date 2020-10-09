package org.kairosdb.metrics4j.configuration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.collectors.impl.ChainedLongCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ParseConfigTest
{
	@BeforeEach
	public void setup()
	{
		MetricConfig metricConfig = MetricConfig.parseConfig("test_config.conf", "Not_there");
		MetricSourceManager.setMetricConfig(metricConfig);
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
}
