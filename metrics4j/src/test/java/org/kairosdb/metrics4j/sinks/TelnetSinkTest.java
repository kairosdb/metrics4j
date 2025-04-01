package org.kairosdb.metrics4j.sinks;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.StringValue;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TelnetSinkTest
{
	@Test
	public void testMillisecond()
	{
		TelnetTestSink sink = new TelnetTestSink(TelnetSink.Resolution.MILLISECONDS);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl()
				.setMethodName("myMethod")
				.setClassName("MyClass")
				.setTime(Instant.ofEpochSecond(962715600));

		reportedMetric.addSample("value", new LongValue(42));

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost");

		FormattedMetric metric = new FormattedMetric(reportedMetric, new HashMap<>(), tags, "");
		metric.addSample(reportedMetric.getSamples().get(0), "FormattedName");

		sink.reportMetrics(Collections.singletonList(metric));

		assertThat(sink.getSentText()).isEqualTo("putm FormattedName 962715600000 42 host=localhost");
	}

	@Test
	public void testSecond()
	{
		TelnetTestSink sink = new TelnetTestSink(TelnetSink.Resolution.SECONDS);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl()
				.setMethodName("myMethod")
				.setClassName("MyClass")
				.setTime(Instant.ofEpochSecond(962715600));

		reportedMetric.addSample("value", new LongValue(42));

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost");

		FormattedMetric metric = new FormattedMetric(reportedMetric, new HashMap<>(), tags, "");
		metric.addSample(reportedMetric.getSamples().get(0), "FormattedName");

		sink.reportMetrics(Collections.singletonList(metric));

		assertThat(sink.getSentText()).isEqualTo("put FormattedName 962715600 42 host=localhost");
	}

	@Test
	public void testString()
	{
		TelnetTestSink sink = new TelnetTestSink(TelnetSink.Resolution.MILLISECONDS);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl()
				.setMethodName("myMethod")
				.setClassName("MyClass")
				.setTime(Instant.ofEpochSecond(962715600));

		reportedMetric.addSample("value", new StringValue("Happy Birthday"));

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost");

		FormattedMetric metric = new FormattedMetric(reportedMetric, new HashMap<>(), tags, "");
		metric.addSample(reportedMetric.getSamples().get(0), "FormattedName");

		sink.reportMetrics(Collections.singletonList(metric));

		assertThat(sink.getSentText()).isEqualTo("puts FormattedName 962715600000 \"Happy Birthday\" host=localhost");
	}

	@Test
	public void testString_withQuotes()
	{
		TelnetTestSink sink = new TelnetTestSink(TelnetSink.Resolution.MILLISECONDS);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl()
				.setMethodName("myMethod")
				.setClassName("MyClass")
				.setTime(Instant.ofEpochSecond(962715600));

		reportedMetric.addSample("value", new StringValue("Happy Birthday \"Brian\""));

		Map<String, String> tags = new HashMap<>();
		tags.put("host", "localhost");

		FormattedMetric metric = new FormattedMetric(reportedMetric, new HashMap<>(), tags, "");
		metric.addSample(reportedMetric.getSamples().get(0), "FormattedName");

		sink.reportMetrics(Collections.singletonList(metric));

		assertThat(sink.getSentText()).isEqualTo("puts FormattedName 962715600000 \"Happy Birthday \\\"Brian\\\"\" host=localhost");
	}

}