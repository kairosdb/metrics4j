package org.kairosdb.metrics4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.annotation.Reported;
import org.kairosdb.metrics4j.annotation.Snapshot;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;
import org.kairosdb.metrics4j.collectors.impl.LastTime;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.collectors.impl.StringReporter;
import org.kairosdb.metrics4j.configuration.ImplementationException;
import org.kairosdb.metrics4j.configuration.TestTrigger;
import org.kairosdb.metrics4j.internal.MetricsContextImpl;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MetricSourceManagerTest
{
	private static class TestCallReport
	{
		private static TestMetricSource reporter = MetricSourceManager.getSource(TestMetricSource.class);

		public void callReport(String host, long value)
		{
			reporter.reportSize(host).put(value);
		}
	}

	@BeforeEach
	void setUp()
	{

	}

	@AfterEach
	public void cleanup()
	{
		MetricSourceManager.clearConfig();
	}

	@Test
	public void test_setStatsForMetric()
	{
		TestMetricSource reporter = MetricSourceManager.getSource(TestMetricSource.class);

		LongCounter myCounter = new LongCounter();

		MetricSourceManager.setCollectorForSource(myCounter, TestMetricSource.class).reportSize("localhost");

		//make sure we get our object back for the same method call
		assertThat(reporter.reportSize("localhost")).isSameAs(myCounter);
		assertThat(reporter.reportSize("127.0.0.1")).isNotSameAs(myCounter);
	}

	@Test
	public void test_booleanParameter()
	{
		TestMetricSource reporter = MetricSourceManager.getSource(TestMetricSource.class);

		LongCounter myCounter = new LongCounter();

		MetricSourceManager.setCollectorForSource(myCounter, TestMetricSource.class).reportStatus(true);

		reporter.reportStatus(true);
	}

	@Test
	public void test_enumParameter()
	{
		TestMetricSource reporter = MetricSourceManager.getSource(TestMetricSource.class);

		LastTime lastTime = new LastTime();

		MetricSourceManager.setCollectorForSource(lastTime, TestMetricSource.class).reportValue(TestMetricSource.TaskType.UPLOAD);

		BlockTimer time = reporter.reportValue(TestMetricSource.TaskType.UPLOAD).time();

		time.close();
	}

	/**
	 This test shows how to use the api with a mock framework to verify
	 certain metrics are being reported from some class being tested.
	 */
	@Test
	public void test_setStatsForMetric_usingMocks()
	{
		//Instantiate some class you want to test
		TestCallReport callReport = new TestCallReport();

		//mock the counter and register it with ReporterFactory for a specific call
		LongCollector myCollector = mock(LongCollector.class);
		MetricSourceManager.setCollectorForSource(myCollector, TestMetricSource.class).reportSize("localhost");

		//Run test class method that will report a value
		callReport.callReport("localhost", 42);

		//Verify value was called on mock
		verify(myCollector).put(42);
	}

	public interface TestMetric
	{
		LongCollector reportLong(@Key("host")String host);
		DoubleCollector reportDouble(@Key("host")String host);
		DurationCollector reportDuration(@Key("host")String host);
	}

	/**
	 Tests when no collectors have been defined for a source
	 */
	@Test
	public void test_unconfiguredSource()
	{
		TestMetric testMetric = MetricSourceManager.getSource(TestMetric.class);

		testMetric.reportDouble("bob").put(3.14);
		testMetric.reportLong("bobby").put(42);
		testMetric.reportDuration("bobster").put(Duration.ofMinutes(5));
	}

	private interface BadMetric
	{
		LongCounter reportCounter();
		LongCollector badParameter(@Key("port")int value);
		LongCollector noAnnotation(String value);
	}

	@Test
	public void test_wrongReturnType()
	{
		BadMetric source = MetricSourceManager.getSource(BadMetric.class);
		ImplementationException exception = assertThrows(ImplementationException.class, () -> {
			source.reportCounter().put(42);
		});

		assertThat(exception.getMessage()).isEqualTo("You have specified a return type on org.kairosdb.metrics4j.MetricSourceManagerTest$BadMetric.reportCounter that is not a generic collector interface as found in org.kairosdb.metrics4j.collectors");
	}

	

	@Test
	public void test_missingAnnotation()
	{
		BadMetric source = MetricSourceManager.getSource(BadMetric.class);
		ImplementationException exception = assertThrows(ImplementationException.class, () -> {
			source.noAnnotation("host").put(42);
		});

		assertThat(exception.getMessage()).isEqualTo("All parameters on org.kairosdb.metrics4j.MetricSourceManagerTest$BadMetric.noAnnotation must be annotated with @Key()");
	}

	@Test
	public void test_addSource_long()
	{
		List<String> rootPath = Collections.emptyList();
		Map<String, String> tags = new HashMap<>();
		MetricsContextImpl context = (MetricsContextImpl) MetricSourceManager.getMetricConfig().getContext();

		TestTrigger trigger = new TestTrigger();
		TestSink sink = new TestSink();
		context.registerTrigger("trigger", trigger);
		context.registerCollector("collector", new LongCounter());
		context.registerSink("sink", sink);


		context.addSinkToPath("sink", rootPath);
		context.addTriggerToPath("trigger", rootPath);
		context.addCollectorToPath("collector", rootPath);

		MetricSourceManager.addSource(new TestLongSource(), tags);

		trigger.triggerCollection(Instant.now());

		assertThat(sink.getResults("longStat").get(0).getValue().getValueAsString()).isEqualTo("42");
	}

	@Test
	public void test_addSource_string()
	{
		List<String> rootPath = Collections.emptyList();
		Map<String, String> tags = new HashMap<>();
		MetricsContextImpl context = (MetricsContextImpl) MetricSourceManager.getMetricConfig().getContext();

		TestTrigger trigger = new TestTrigger();
		TestSink sink = new TestSink();
		context.registerTrigger("trigger", trigger);
		context.registerCollector("collector", new StringReporter());
		context.registerSink("sink", sink);


		context.addSinkToPath("sink", rootPath);
		context.addTriggerToPath("trigger", rootPath);
		context.addCollectorToPath("collector", rootPath);

		MetricSourceManager.addSource(new TestStringSource(), tags);

		trigger.triggerCollection(Instant.now());

		assertThat(sink.getResults("stringStat").get(0).getValue().getValueAsString()).isEqualTo("Happy");
	}


	public static class TestLongSource
	{
		@Reported(help = "help text", field = "my_value")
		public long longStat()
		{
			return 42L;
		}
	}


	public static class TestStringSource
	{
		boolean snapshot = false;
		@Snapshot
		public void callMeFirst()
		{
			snapshot = true;
		}

		@Reported(help = "help text", field = "my_value")
		public String stringStat()
		{
			assertThat(snapshot).isTrue();
			return "Happy";
		}
	}

}