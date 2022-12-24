package org.kairosdb.metrics4j.plugins;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.configuration.TestTrigger;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.internal.MetricsContextImpl;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.mockito.ArgumentCaptor;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class JMXReporterTest
{
	private MetricsContextImpl m_context;
	private MetricConfig m_metricConfig;
	private TestTrigger m_testTrigger;
	private MetricSink m_sink;

	@BeforeEach
	public void setup()
	{
		m_context = new MetricsContextImpl();
		m_metricConfig = new MetricConfig(m_context);
		MetricSourceManager.setMetricConfig(m_metricConfig);
		m_testTrigger = new TestTrigger();

		m_sink = mock(MetricSink.class);

		when(m_sink.getDefaultFormatter()).thenReturn(new DefaultFormatter());

		m_context.registerTrigger("trigger", m_testTrigger);
		m_context.registerFormatter("formatter", new TestFormatter());
		m_context.registerSink("sink1", m_sink);

		List<String> rootPath = createPath();
		m_context.addSinkToPath("sink1", rootPath);
		m_context.addTriggerToPath("trigger", rootPath);
		m_context.addFormatterToPath("formatter", rootPath);

	}

	@AfterEach
	public void cleanup()
	{
		MetricSourceManager.clearConfig();
	}

	private List<String> createPath(String... paths)
	{
		List<String> ret = new ArrayList<>();
		for (String path : paths)
		{
			ret.add(path);
		}

		return ret;
	}

	/*@Test
	void main()
	{
		JMXReporter.main(new String[0]);
	}*/


	@Test
	void testBeanRegistration() throws InstanceNotFoundException, MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, IOException
	{
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		JMXReporter reporter = new JMXReporter(server);
		reporter.setClassNameAttributes(asList("type"));
		reporter.addMBeanNotification();

		ObjectName name = new ObjectName("org.kairosdb.jmxreporter:type=Test");
		TestImpl test = new TestImpl();
		server.registerMBean(test, name);

		Instant now = Instant.now();
		m_testTrigger.triggerCollection(now);

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(m_sink).reportMetrics(captor.capture());
		verifyNoMoreInteractions(m_sink);

		//assertThat(captor.getValue().size()).isEqualTo(4);

		Map<String, String> expectedTags = new HashMap<>();

		List<String> methods = new ArrayList<>();
		List<MetricValue> values = new ArrayList<>();
		List<String> metricNames = new ArrayList<>();

		List<FormattedMetric> reportedMetrics = captor.getValue();
		for (FormattedMetric reportedMetric : reportedMetrics)
		{
			assertThat(reportedMetric.getClassName()).isEqualTo("org.kairosdb.jmxreporter.Test");
			methods.add(reportedMetric.getMethodName());
			assertThat(reportedMetric.getTags()).containsExactlyInAnyOrderEntriesOf(expectedTags);
			for (FormattedMetric.Sample sample : reportedMetric.getSamples())
			{
				assertThat(sample.getFieldName()).isEqualTo("value");
				values.add(sample.getValue());
				metricNames.add(sample.getMetricName());
			}
		}

		assertThat(methods).containsExactlyInAnyOrder("IntCount", "LongCount", "FloatValue", "DoubleValue");
		assertThat(values).containsExactlyInAnyOrder(new LongValue(1), new LongValue(123), new DoubleValue(3.14159), new DoubleValue(1.1f));
		assertThat(metricNames).containsExactlyInAnyOrder("org.kairosdb.jmxreporter.Test.IntCount.value",
				"org.kairosdb.jmxreporter.Test.LongCount.value",
				"org.kairosdb.jmxreporter.Test.FloatValue.value",
				"org.kairosdb.jmxreporter.Test.DoubleValue.value");

		server.unregisterMBean(name);
		reporter.close();
	}


	@Test
	void testCompositBeanRegistration() throws InstanceNotFoundException, MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException, ListenerNotFoundException, IOException
	{
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();

		JMXReporter reporter = new JMXReporter(server);
		reporter.setClassNameAttributes(asList("type"));
		reporter.addMBeanNotification();

		ObjectName name = new ObjectName("org.kairosdb.jmxreporter:type=TestComposite,kind=grass");
		TestCompositImpl test = new TestCompositImpl();
		server.registerMBean(test, name);

		Instant now = Instant.now();
		m_testTrigger.triggerCollection(now);

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(m_sink).reportMetrics(captor.capture());
		verifyNoMoreInteractions(m_sink);

		assertThat(captor.getValue().size()).isEqualTo(1);

		Map<String, String> expectedTags = new HashMap<>();
		expectedTags.put("kind", "grass");

		List<String> methods = new ArrayList<>();
		List<MetricValue> values = new ArrayList<>();
		List<String> metricNames = new ArrayList<>();

		List<FormattedMetric> reportedMetrics = captor.getValue();
		for (FormattedMetric reportedMetric : reportedMetrics)
		{
			assertThat(reportedMetric.getClassName()).isEqualTo("org.kairosdb.jmxreporter.TestComposite");
			assertThat(reportedMetric.getMethodName()).isEqualTo("CompositeValues");
			assertThat(reportedMetric.getTags()).containsExactlyInAnyOrderEntriesOf(expectedTags);
			for (FormattedMetric.Sample sample : reportedMetric.getSamples())
			{
				values.add(sample.getValue());
				metricNames.add(sample.getMetricName());
			}
		}

		assertThat(values).containsExactlyInAnyOrder(new LongValue(42), new LongValue(1234), new DoubleValue(3.14159), new DoubleValue(1.1f));
		assertThat(metricNames).containsExactlyInAnyOrder("org.kairosdb.jmxreporter.TestComposite.CompositeValues.myInt",
				"org.kairosdb.jmxreporter.TestComposite.CompositeValues.myLong",
				"org.kairosdb.jmxreporter.TestComposite.CompositeValues.myFloat",
				"org.kairosdb.jmxreporter.TestComposite.CompositeValues.myDouble");

		server.unregisterMBean(name);
		reporter.close();

	}
}
