package org.kairosdb.metrics4j;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.DoubleCounter;
import org.kairosdb.metrics4j.collectors.LongCounter;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.configuration.TestFormatter;
import org.kairosdb.metrics4j.configuration.TestSource;
import org.kairosdb.metrics4j.configuration.TestTrigger;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
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
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class Metrics4jTest
{
	private MetricConfig m_metricConfig;
	private TestTrigger m_testTrigger;
	private MetricSink m_sink1;

	@BeforeEach
	public void registerComponents() throws IOException, SAXException, ParserConfigurationException
	{
		InputStream propsIs = ClassLoader.getSystemClassLoader().getResourceAsStream("test_props.properties");
		Properties props = new Properties();
		props.load(propsIs);

		m_metricConfig = MetricConfig.parseConfig("test_config");
		MetricsContext context = m_metricConfig.getContext();
		m_metricConfig.setProperties(props);
		MetricSourceManager.setMetricConfig(m_metricConfig);
		m_testTrigger = new TestTrigger();

		m_sink1 = mock(MetricSink.class);

		when(m_sink1.getDefaultFormatter()).thenReturn(new DefaultFormatter());

		context.registerTrigger("trigger", m_testTrigger);
		context.addTriggerToPath("trigger", new ArrayList<>());
		context.registerFormatter("formatter", new TestFormatter());
		context.registerCollector("long", new LongCounter());
		context.registerCollector("double", new DoubleCounter());

		context.registerSink("sink1", m_sink1);
		context.addSinkToPath("sink1", new ArrayList<>());
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

		ReportedMetricImpl expected = new ReportedMetricImpl();
		expected.setClassName("org.kairosdb.metrics4j.configuration.TestSource")
				.setMethodName("countSomething")
				//.setMetricName("my_metric.count_something")
				.setTime(now)
				.setTags(new HashMap<>())
				//.setProps(Collections.singletonMap("statsd:type", "c"))
				.addSample("count", new LongValue(42));

		FormattedMetric formattedMetric = new FormattedMetric(expected,
				Collections.singletonMap("statsd_type", "c"), tags);
		formattedMetric.addSample(expected.getSamples().get(0), "my_metric.count_something.count");

		verify(m_sink1).reportMetrics(Collections.singletonList(formattedMetric));
	}

	@Test
	public void test_metricNameAttribute() throws IOException, SAXException, ParserConfigurationException
	{

	}

	@Test
	public void test_mergeConfigs()
	{
		Config config1 = ConfigFactory.parseResources("merge.conf");
		Config config2 = ConfigFactory.parseResources("merge.properties");
		Config resolve = config2.withFallback(config1).resolve();
		System.out.println(resolve.getString("value-two"));
		System.out.println(resolve.getString("value-one"));
		System.out.println(resolve.getString("system-value"));
	}
}
