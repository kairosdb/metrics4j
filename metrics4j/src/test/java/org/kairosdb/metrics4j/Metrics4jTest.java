package org.kairosdb.metrics4j;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.DoubleCounter;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.configuration.TestFormatter;
import org.kairosdb.metrics4j.configuration.TestSource;
import org.kairosdb.metrics4j.configuration.TestTrigger;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_CUMULATIVE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;
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

		m_metricConfig = MetricConfig.parseConfig("test_config.conf", "Not_there");
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

	@AfterEach
	public void cleanup()
	{
		MetricSourceManager.clearConfig();
	}

	@Test
	public void test_additionalTags()
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);

		source.countSomething().put(42);

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost_override");
		tags.put("datacenter", "dc-aws");

		Map<String, String> reportContext = new HashMap<>();
		reportContext.put(AGGREGATION_KEY, AGGREGATION_CUMULATIVE_VALUE);
		reportContext.put(TYPE_KEY, TYPE_COUNTER_VALUE);

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
		expected.setContext(reportContext);

		FormattedMetric formattedMetric = new FormattedMetric(expected,
				Collections.singletonMap("statsd_type", "c"), tags, "");
		formattedMetric.addSample(expected.getSamples().get(0), "my_metric.count_something.count");

		verify(m_sink1).reportMetrics(Collections.singletonList(formattedMetric));
	}

	@Test
	public void test_tagOverride()
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);

		//we report twice with different tags - these are overridden to be bob
		//and will get aggregated together instead of reported separately.
		source.countOverride("client1").put(5);
		source.countOverride("client2").put(5);

		Instant now = Instant.now();
		m_testTrigger.triggerCollection(now);

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost_override");
		tags.put("datacenter", "dc-aws");
		tags.put("client", "bob");

		Map<String, String> reportContext = new HashMap<>();
		reportContext.put(AGGREGATION_KEY, AGGREGATION_CUMULATIVE_VALUE);
		reportContext.put(TYPE_KEY, TYPE_COUNTER_VALUE);

		ReportedMetricImpl expected = new ReportedMetricImpl();
		expected.setClassName("org.kairosdb.metrics4j.configuration.TestSource")
				.setMethodName("countOverride")
				.setTime(now)
				.setTags(Collections.singletonMap("client", "bob")) //Reported as single value
				.addSample("count", new LongValue(10));
		expected.setContext(reportContext);

		FormattedMetric formattedMetric = new FormattedMetric(expected,
				Collections.emptyMap(), tags, "");
		formattedMetric.addSample(expected.getSamples().get(0), "metric4j.org.kairosdb.metrics4j.configuration.TestSource.countOverride.count");

		verify(m_sink1).reportMetrics(Collections.singletonList(formattedMetric));
	}

	@Test
	public void test_tagNoOverride()
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);

		//client tag is not overridden so it should report two values
		source.countNoOverride("client1").put(5);
		source.countNoOverride("client2").put(5);

		Instant now = Instant.now();
		m_testTrigger.triggerCollection(now);

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost_override");
		tags.put("datacenter", "dc-aws");

		Map<String, String> reportContext = new HashMap<>();
		reportContext.put(AGGREGATION_KEY, AGGREGATION_CUMULATIVE_VALUE);
		reportContext.put(TYPE_KEY, TYPE_COUNTER_VALUE);

		ReportedMetricImpl expected1 = new ReportedMetricImpl();
		expected1.setClassName("org.kairosdb.metrics4j.configuration.TestSource")
				.setMethodName("countNoOverride")
				.setTime(now)
				.setTags(Collections.singletonMap("client", "client1"))
				.addSample("count", new LongValue(5));
		expected1.setContext(reportContext);

		FormattedMetric formattedMetric1 = new FormattedMetric(expected1,
				Collections.emptyMap(), tags, "");
		formattedMetric1.addSample(expected1.getSamples().get(0), "metric4j.org.kairosdb.metrics4j.configuration.TestSource.countNoOverride.count");

		ReportedMetricImpl expected2 = new ReportedMetricImpl();
		expected2.setClassName("org.kairosdb.metrics4j.configuration.TestSource")
				.setMethodName("countNoOverride")
				.setTime(now)
				.setTags(Collections.singletonMap("client", "client2"))
				.addSample("count", new LongValue(5));
		expected2.setContext(reportContext);

		FormattedMetric formattedMetric2 = new FormattedMetric(expected2,
				Collections.emptyMap(), tags, "");
		formattedMetric2.addSample(expected2.getSamples().get(0), "metric4j.org.kairosdb.metrics4j.configuration.TestSource.countNoOverride.count");

		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
		verify(m_sink1).reportMetrics(argument.capture());

		assertThat(argument.getValue()).containsExactlyInAnyOrder(formattedMetric1, formattedMetric2);
	}

	@Test
	public void test_metricContext()
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);

		source.testContext("my_tag").put(42);

		Instant now = Instant.now();
		m_testTrigger.triggerCollection(now);

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost_override");
		tags.put("datacenter", "dc-aws");

		Map<String, String> reportContext = new HashMap<>();
		reportContext.put(AGGREGATION_KEY, AGGREGATION_DELTA_VALUE);
		reportContext.put(TYPE_KEY, TYPE_COUNTER_VALUE);

		ReportedMetricImpl expected1 = new ReportedMetricImpl();
		expected1.setClassName("org.kairosdb.metrics4j.configuration.TestSource")
				.setMethodName("testContext")
				.setTime(now)
				.setTags(Collections.singletonMap("tag", "my_tag"))
				.addSample("count", new LongValue(42));
		expected1.setContext(reportContext);

		FormattedMetric formattedMetric1 = new FormattedMetric(expected1,
				Collections.emptyMap(), tags, "");
		formattedMetric1.addSample(expected1.getSamples().get(0), "metric4j.org.kairosdb.metrics4j.configuration.TestSource.testContext.count");


		ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
		verify(m_sink1).reportMetrics(argument.capture());

		System.out.println(((FormattedMetric)argument.getValue().get(0)).getContext());

		assertThat(argument.getValue()).containsExactlyInAnyOrder(formattedMetric1);
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
