package org.kairosdb.metrics4j.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.collectors.DoubleCounter;
import org.kairosdb.metrics4j.collectors.LongCounter;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.mockito.ArgumentCaptor;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kairosdb.metrics4j.configuration.MetricConfig.appendSourceName;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class MetricConfigTest
{
	private MetricConfig m_metricConfig;
	private TestTrigger m_testTrigger;
	private MetricSink m_sink1;

	@BeforeEach
	public void registerComponents()
	{
		m_metricConfig = new MetricConfig();
		MetricSourceManager.setMetricConfig(m_metricConfig);
		m_testTrigger = new TestTrigger();

		m_sink1 = mock(MetricSink.class);

		m_metricConfig.registerTrigger("trigger", m_testTrigger);
		m_metricConfig.registerFormatter("formatter", new TestFormatter());
		m_metricConfig.registerCollector("long", new LongCounter());
		m_metricConfig.registerCollector("double", new DoubleCounter());
		m_metricConfig.registerSink("sink1", m_sink1);
	}


	@Test
	public void testReadingConfiguration() throws IOException, SAXException, ParserConfigurationException
	{
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("test_config.xml");

		MetricConfig metricConfig = MetricConfig.parseConfig(null, is);

		System.out.println(metricConfig.getSink("slf4j"));

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


	@Test
	public void testDoubleCollectors()
	{
		List<String> rootPath = createPath();
		m_metricConfig.addSinkToPath("sink1", rootPath);
		m_metricConfig.addTriggerToPath("trigger", rootPath);
		m_metricConfig.addFormatterToPath("formatter", rootPath);
		m_metricConfig.addCollectorToPath("long", rootPath);
		m_metricConfig.addCollectorToPath("double", createPath("org", "kairosdb"));

		TestSource source = MetricSourceManager.getSource(TestSource.class);

		source.countSomething().put(1);
		Instant now = Instant.now();

		m_testTrigger.triggerCollection(now);

		ReportedMetric metric = new ReportedMetricImpl()
				.setFieldName("count")
				.setMetricName("org.kairosdb.metrics4j.configuration.TestSource.countSomething.count")
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomething")
				.setValue(new LongValue(1))
				.setTags(new HashMap<>())
				.setProps(new HashMap<>());

		verify(m_sink1).init(any());

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		verify(m_sink1).reportMetrics(captor.capture());
		verifyNoMoreInteractions(m_sink1);

		assertThat(captor.getValue().get(0)).isEqualToComparingFieldByFieldRecursively(metric);
	}

	@Test
	public void test_ensureCollectorsAreCloned()
	{
		List<String> rootPath = createPath();
		m_metricConfig.addSinkToPath("sink1", rootPath);
		m_metricConfig.addTriggerToPath("trigger", rootPath);
		m_metricConfig.addFormatterToPath("formatter", rootPath);
		m_metricConfig.addCollectorToPath("long", rootPath);

		TestSource source = MetricSourceManager.getSource(TestSource.class);

		LongCounter counter1 = source.countSomething();
		LongCounter counter2 = source.countSomethingElse();

		assertThat(counter1).isNotEqualTo(counter2);
	}

	@Test
	public void test_appendSourceName()
	{
		List<String> path = createPath("org", "kairosdb");

		List<String> newPath = appendSourceName(path, "test.MyClass.myMethod");

		assertThat(newPath).containsExactly("org", "kairosdb", "test", "MyClass", "myMethod");
	}


}