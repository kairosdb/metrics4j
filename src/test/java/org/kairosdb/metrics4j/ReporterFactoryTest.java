package org.kairosdb.metrics4j;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.stats.Counter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReporterFactoryTest
{
	private static class TestCallReport
	{
		private static TestReporter reporter = ReporterFactory.getReporter(TestReporter.class);

		public void callReport(String host, long value)
		{
			reporter.reportSize(host).add(value);
		}
	}

	@org.junit.jupiter.api.BeforeEach
	void setUp()
	{



	}

	@Test
	public void test_setStatsForMetric()
	{
		TestReporter reporter = ReporterFactory.getReporter(TestReporter.class);

		Counter myCounter = new Counter();

		ReporterFactory.setStatsForMetric(myCounter, TestReporter.class).reportSize("localhost");

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
		Counter myCounter = mock(Counter.class);
		ReporterFactory.setStatsForMetric(myCounter, TestReporter.class).reportSize("localhost");

		//Run test class method that will report a value
		callReport.callReport("localhost", 42);

		//Verify value was called on mock
		verify(myCounter).add(42);
	}



}