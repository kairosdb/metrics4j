package org.kairosdb.metrics4j.internal;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.StringCollector;
import org.kairosdb.metrics4j.collectors.TimeCollector;
import org.kairosdb.metrics4j.internal.testing.VerifyException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectorRecorderTest
{

	public interface TestStats
	{
		DoubleCollector testPutDouble(@Key("test-key")String key);
		LongCollector testPutLong(@Key("test-key")String key);
		DurationCollector testPutDuration(@Key("test-key")String key);
		StringCollector testPutString(@Key("test-key")String key);
		TimeCollector testPutInstant(@Key("test-key")String key);
	}

	public static TestStats stats = MetricSourceManager.getSource(TestStats.class);
	public static final String KEY = "my_key";
	private final Instant NOW = Instant.now();

	@Test
	void testPutDouble()
	{
		MetricSourceManager.record(TestStats.class);

		stats.testPutDouble(KEY).put(123.456);
		stats.testPutDouble(KEY).put(NOW,123.456);

		MetricSourceManager.verify(TestStats.class).testPutDouble(KEY).put(123.456);
		MetricSourceManager.verify(TestStats.class).testPutDouble(KEY).put(NOW, 123.456);
	}

	@Test
	void testPutLong()
	{
		MetricSourceManager.record(TestStats.class);

		stats.testPutLong(KEY).put(123456);
		stats.testPutLong(KEY).put(NOW, 123456);

		MetricSourceManager.verify(TestStats.class).testPutLong(KEY).put(123456);
		MetricSourceManager.verify(TestStats.class).testPutLong(KEY).put(NOW, 123456);
	}

	@Test
	void testPutDuration()
	{
		MetricSourceManager.record(TestStats.class);
		Duration duration = Duration.ofMillis(42);

		stats.testPutDuration(KEY).put(duration);
		stats.testPutDuration(KEY).put(NOW, duration);

		MetricSourceManager.verify(TestStats.class).testPutDuration(KEY).put(duration);
		MetricSourceManager.verify(TestStats.class).testPutDuration(KEY).put(NOW, duration);
	}

	@Test
	void testPutString()
	{
		MetricSourceManager.record(TestStats.class);
		String value = "Hello World";

		stats.testPutString(KEY).put(value);
		stats.testPutString(KEY).put(NOW, value);

		MetricSourceManager.verify(TestStats.class).testPutString(KEY).put(value);
		MetricSourceManager.verify(TestStats.class).testPutString(KEY).put(NOW, value);
	}

	@Test
	void testPutInstant()
	{
		MetricSourceManager.record(TestStats.class);
		Instant time = Instant.ofEpochSecond(123456);

		stats.testPutInstant(KEY).put(time);
		stats.testPutInstant(KEY).put(NOW, time);

		MetricSourceManager.verify(TestStats.class).testPutInstant(KEY).put(time);
		MetricSourceManager.verify(TestStats.class).testPutInstant(KEY).put(NOW, time);
	}

	@Test
	void testPutLongCountFailure()
	{
		MetricSourceManager.record(TestStats.class);

		stats.testPutLong(KEY).put(123456);
		stats.testPutLong(KEY).put(123456);

		assertThatThrownBy(() -> {
					MetricSourceManager.verify(TestStats.class).testPutLong(KEY).put(123456);
				}
		).isInstanceOf(VerifyException.class)
				.hasMessage("Wanted 1 calls but received 2");

		assertThatThrownBy(() -> {
					MetricSourceManager.verify(TestStats.class).testPutLong(KEY).put(789);
				}
		).isInstanceOf(VerifyException.class)
				.hasMessage("Wanted 1 calls but received 0");
	}

	@Test
	void testPutLongDifferntValues()
	{
		MetricSourceManager.record(TestStats.class);

		stats.testPutLong(KEY).put(123456);
		stats.testPutLong(KEY).put(789);

		MetricSourceManager.verify(TestStats.class).testPutLong(KEY).put(123456);
		MetricSourceManager.verify(TestStats.class).testPutLong(KEY).put(789);
	}

	@Test
	void testPut6()
	{
	}

	@Test
	void testPut7()
	{
	}

	@Test
	void testPut8()
	{
	}
}