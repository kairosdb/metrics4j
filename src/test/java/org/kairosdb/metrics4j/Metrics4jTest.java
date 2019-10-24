package org.kairosdb.metrics4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.DoubleCounter;
import org.kairosdb.metrics4j.collectors.LongCounter;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.configuration.TestFormatter;
import org.kairosdb.metrics4j.configuration.TestSource;
import org.kairosdb.metrics4j.configuration.TestTrigger;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Metrics4jTest
{
	private MetricConfig m_metricConfig;
	private TestTrigger m_testTrigger;
	private MetricSink m_sink1;

	@BeforeEach
	public void registerComponents() throws IOException, SAXException, ParserConfigurationException
	{
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("test_config.xml");
		InputStream propsIs = ClassLoader.getSystemClassLoader().getResourceAsStream("test_props.properties");

		m_metricConfig = MetricConfig.parseConfig(propsIs, is);
		MetricSourceManager.setMetricConfig(m_metricConfig);
		m_testTrigger = new TestTrigger();

		m_sink1 = mock(MetricSink.class);

		m_metricConfig.registerTrigger("trigger", m_testTrigger);
		m_metricConfig.addTriggerToPath("trigger", new ArrayList<>());
		m_metricConfig.registerFormatter("formatter", new TestFormatter());
		m_metricConfig.registerCollector("long", new LongCounter());
		m_metricConfig.registerCollector("double", new DoubleCounter());

		m_metricConfig.registerSink("sink1", m_sink1);
		m_metricConfig.addSinkToPath("sink1", new ArrayList<>());
	}

	@Test
	public void test_additionalTags()
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);

		source.countSomething().put(42);

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost_override");
		tags.put("datacenter", "dc-aws");

		Instant now = Instant.now();
		m_testTrigger.triggerCollection(now);

		ReportedMetric expected = new ReportedMetricImpl();
		expected.setClassName("org.kairosdb.metrics4j.configuration.TestSource")
				.setMethodName("countSomething")
				.setMetricName("my_metric.count_something")
				.setTags(tags)
				.setProps(Collections.singletonMap("statsd:type", "c"))
				.setFieldName("count")
				.setValue(new LongValue(42))
				.setTime(now);

		verify(m_sink1).reportMetrics(Collections.singletonList(expected));
	}

	@Test
	public void test_metricNameAttribute() throws IOException, SAXException, ParserConfigurationException
	{

	}
}
