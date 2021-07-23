package org.kairosdb.metrics4j.collectors;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.TestMetricSource;
import org.kairosdb.metrics4j.collectors.helpers.BlockTimer;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;
import org.kairosdb.metrics4j.internal.DevNullCollector;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class DurationCollectorTest
{
	private String longOperation()
	{
		try
		{
			Thread.sleep(200);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return ("sam");
	}

	@Test
	public void testLambda()
	{
		TestMetricSource reporter = MetricSourceManager.getSource(TestMetricSource.class);

		LongCounter myCounter = new LongCounter();
		DurationCollector collector = spy(new DevNullCollector());

		MetricSourceManager.setCollectorForSource(collector, TestMetricSource.class).reportTime("localhost");

		String response = reporter.reportTime("localhost").time(() -> longOperation());

		//make sure we get our object back for the same method call
		assertThat(response).isEqualTo("sam");
		verify(collector).put(argThat(argument -> (argument.compareTo(Duration.ofMillis(200)) >= 0)));
	}

	@Test
	public void testBlockTimer()
	{
		TestMetricSource reporter = MetricSourceManager.getSource(TestMetricSource.class);

		LongCounter myCounter = new LongCounter();
		DurationCollector collector = spy(new DevNullCollector());

		MetricSourceManager.setCollectorForSource(collector, TestMetricSource.class).reportTime("localhost");
		String response;

		try (BlockTimer ignored = reporter.reportTime("localhost").time())
		{
			response = longOperation();
		}

		//make sure we get our object back for the same method call
		assertThat(response).isEqualTo("sam");
		verify(collector).put(argThat(argument -> (argument.compareTo(Duration.ofMillis(200)) >= 0)));
	}
}
