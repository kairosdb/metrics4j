package org.kairosdb.metrics4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.collectors.impl.BagCollector;
import org.kairosdb.metrics4j.collectors.impl.DoubleCounter;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.configuration.MetricConfig;
import org.kairosdb.metrics4j.configuration.TestFormatter;
import org.kairosdb.metrics4j.configuration.TestSource;
import org.kairosdb.metrics4j.configuration.TestTrigger;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.internal.MetricsContextImpl;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.kairosdb.metrics4j.configuration.MetricConfig.CONFIG_SYSTEM_PROPERTY;
import static org.kairosdb.metrics4j.configuration.MetricConfig.OVERRIDES_SYSTEM_PROPERTY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class MetricThreadHelperTest
{
	private MetricsContextImpl m_context;
	private MetricConfig m_metricConfig;
	private TestTrigger m_testTrigger;
	private MetricSink m_sink1;

	@BeforeEach
	public void registerComponents()
	{
		m_context = new MetricsContextImpl();
		m_metricConfig = new MetricConfig(m_context);
		MetricSourceManager.setMetricConfig(m_metricConfig);
		m_testTrigger = new TestTrigger();

		m_sink1 = mock(MetricSink.class);

		when(m_sink1.getDefaultFormatter()).thenReturn(new DefaultFormatter());

		m_context.registerTrigger("trigger", m_testTrigger);
		m_context.registerFormatter("formatter", new TestFormatter());
		m_context.registerCollector("long", new LongCounter());
		m_context.registerCollector("bag", new BagCollector());
		m_context.registerSink("sink1", m_sink1);
	}

	@AfterEach
	public void cleanup()
	{
		MetricSourceManager.clearConfig();
		System.clearProperty(CONFIG_SYSTEM_PROPERTY);
		System.clearProperty(OVERRIDES_SYSTEM_PROPERTY);
	}

	private void downstreamMetricReport(Instant now, long value)
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);
		source.countSomething().put(now, value);
	}

	private void downstreamMetricReport(long value)
	{
		TestSource source = MetricSourceManager.getSource(TestSource.class);
		source.countSomethingWithTag("awayhost").put(value);
	}

	@Test
	void test_setReportTime() throws InterruptedException
	{
		List<String> rootPath = Collections.emptyList();
		m_context.addSinkToPath("sink1", rootPath);
		m_context.addTriggerToPath("trigger", rootPath);
		m_context.addFormatterToPath("formatter", rootPath);
		m_context.addCollectorToPath("bag", rootPath); //Set report time only works on a bag

		Instant now = Instant.now();
		Instant reportTime = Instant.ofEpochMilli(now.toEpochMilli() - 1000);

		//set report time on thread
		MetricThreadHelper.setReportTime(reportTime);

		downstreamMetricReport(now, 1);

		//Clear set time
		MetricThreadHelper.clearReportTime();

		downstreamMetricReport(now, 2);

		m_testTrigger.triggerCollection(now);

		ReportedMetric metric = new ReportedMetricImpl()
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomething")
				.setTags(new HashMap<>())
				.addSample("value", new LongValue(1)).setTime(reportTime).reportedMetric()
				.addSample("value", new LongValue(2)).setTime(now).reportedMetric();

		FormattedMetric formattedMetric = new FormattedMetric(metric,
				new HashMap<>(), new HashMap<>(), "");
		formattedMetric.addSample(metric.getSamples().get(0), "org.kairosdb.metrics4j.configuration.TestSource.countSomething.value");
		formattedMetric.addSample(metric.getSamples().get(1), "org.kairosdb.metrics4j.configuration.TestSource.countSomething.value");

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		verify(m_sink1).reportMetrics(captor.capture());
		verifyNoMoreInteractions(m_sink1);

		assertThat(captor.getValue()).containsOnly(formattedMetric);
	}

	@Test
	void test_addTag()
	{
		List<String> rootPath = Collections.emptyList();
		m_context.addSinkToPath("sink1", rootPath);
		m_context.addTriggerToPath("trigger", rootPath);
		m_context.addFormatterToPath("formatter", rootPath);
		m_context.addCollectorToPath("long", rootPath); //Set report time only works on a bag

		Instant now = Instant.now();

		MetricThreadHelper.addTag("my", "tag");

		downstreamMetricReport(now, 1);

		MetricThreadHelper.clearTags();

		downstreamMetricReport(now, 2);

		m_testTrigger.triggerCollection(now);

		Map<String, String> myTags = new HashMap<>();
		myTags.put("my", "tag");

		ReportedMetric metric1 = new ReportedMetricImpl()
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomething")
				.setTags(myTags)
				.addSample("count", new LongValue(1)).reportedMetric();

		FormattedMetric formattedMetric = new FormattedMetric(metric1,
				new HashMap<>(), new HashMap<>(), "");
		formattedMetric.addSample(metric1.getSamples().get(0), "org.kairosdb.metrics4j.configuration.TestSource.countSomething.count");

		ReportedMetric metric2 = new ReportedMetricImpl()
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomething")
				.setTags(new HashMap<>())
				.addSample("count", new LongValue(2)).reportedMetric();

		FormattedMetric formattedMetric2 = new FormattedMetric(metric2,
				new HashMap<>(), new HashMap<>(), "");
		formattedMetric2.addSample(metric2.getSamples().get(0), "org.kairosdb.metrics4j.configuration.TestSource.countSomething.count");

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		verify(m_sink1).reportMetrics(captor.capture());
		verifyNoMoreInteractions(m_sink1);

		assertThat(captor.getValue()).containsExactlyInAnyOrder(formattedMetric, formattedMetric2);
	}


	@Test
	void test_addTag_withTags()
	{
		List<String> rootPath = Collections.emptyList();
		m_context.addSinkToPath("sink1", rootPath);
		m_context.addTriggerToPath("trigger", rootPath);
		m_context.addFormatterToPath("formatter", rootPath);
		m_context.addCollectorToPath("long", rootPath); //Set report time only works on a bag

		Instant now = Instant.now();

		MetricThreadHelper.addTag("my", "tag");

		downstreamMetricReport(1);

		MetricThreadHelper.clearTags();

		downstreamMetricReport(2);

		m_testTrigger.triggerCollection(now);

		Map<String, String> myTags = new HashMap<>();
		myTags.put("my", "tag");
		myTags.put("host", "awayhost");

		ReportedMetric metric1 = new ReportedMetricImpl()
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomethingWithTag")
				.setTags(myTags)
				.addSample("count", new LongValue(1)).reportedMetric();

		FormattedMetric formattedMetric = new FormattedMetric(metric1,
				new HashMap<>(), new HashMap<>(), "");
		formattedMetric.addSample(metric1.getSamples().get(0), "org.kairosdb.metrics4j.configuration.TestSource.countSomethingWithTag.count");

		Map<String, String> myTags2 = new HashMap<>();
		myTags2.put("host", "awayhost");

		ReportedMetric metric2 = new ReportedMetricImpl()
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomethingWithTag")
				.setTags(myTags2)
				.addSample("count", new LongValue(2)).reportedMetric();

		FormattedMetric formattedMetric2 = new FormattedMetric(metric2,
				new HashMap<>(), new HashMap<>(), "");
		formattedMetric2.addSample(metric2.getSamples().get(0), "org.kairosdb.metrics4j.configuration.TestSource.countSomethingWithTag.count");

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		verify(m_sink1).reportMetrics(captor.capture());
		verifyNoMoreInteractions(m_sink1);

		assertThat(captor.getValue()).containsExactlyInAnyOrder(formattedMetric, formattedMetric2);
	}

	@Test
	void test_addTag_override()
	{
		List<String> rootPath = Collections.emptyList();
		m_context.addSinkToPath("sink1", rootPath);
		m_context.addTriggerToPath("trigger", rootPath);
		m_context.addFormatterToPath("formatter", rootPath);
		m_context.addCollectorToPath("long", rootPath); //Set report time only works on a bag

		Instant now = Instant.now();

		MetricThreadHelper.addTag("host", "overrideHost");

		downstreamMetricReport(1);

		MetricThreadHelper.clearTags();

		downstreamMetricReport(2);

		m_testTrigger.triggerCollection(now);

		Map<String, String> myTags = new HashMap<>();
		myTags.put("host", "overrideHost");

		ReportedMetric metric1 = new ReportedMetricImpl()
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomethingWithTag")
				.setTags(myTags)
				.addSample("count", new LongValue(1)).reportedMetric();

		FormattedMetric formattedMetric = new FormattedMetric(metric1,
				new HashMap<>(), new HashMap<>(), "");
		formattedMetric.addSample(metric1.getSamples().get(0), "org.kairosdb.metrics4j.configuration.TestSource.countSomethingWithTag.count");

		Map<String, String> myTags2 = new HashMap<>();
		myTags2.put("host", "awayhost");

		ReportedMetric metric2 = new ReportedMetricImpl()
				.setTime(now)
				.setClassName(TestSource.class.getName())
				.setMethodName("countSomethingWithTag")
				.setTags(myTags2)
				.addSample("count", new LongValue(2)).reportedMetric();

		FormattedMetric formattedMetric2 = new FormattedMetric(metric2,
				new HashMap<>(), new HashMap<>(), "");
		formattedMetric2.addSample(metric2.getSamples().get(0), "org.kairosdb.metrics4j.configuration.TestSource.countSomethingWithTag.count");

		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

		verify(m_sink1).reportMetrics(captor.capture());
		verifyNoMoreInteractions(m_sink1);

		assertThat(captor.getValue()).containsExactlyInAnyOrder(formattedMetric, formattedMetric2);
	}
}