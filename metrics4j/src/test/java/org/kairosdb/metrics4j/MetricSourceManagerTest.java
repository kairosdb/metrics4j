package org.kairosdb.metrics4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.configuration.ImplementationException;

import java.time.Duration;

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
	public void test_wrongParamType()
	{
		BadMetric source = MetricSourceManager.getSource(BadMetric.class);
		ImplementationException exception = assertThrows(ImplementationException.class, () -> {
			source.badParameter(42).put(42);
		});

		assertThat(exception.getMessage()).isEqualTo("All parameters on org.kairosdb.metrics4j.MetricSourceManagerTest$BadMetric.badParameter must be of type String");
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



}