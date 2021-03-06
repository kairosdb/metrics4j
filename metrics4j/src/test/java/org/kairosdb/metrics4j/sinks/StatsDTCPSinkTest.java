package org.kairosdb.metrics4j.sinks;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.internal.FormattedMetric;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StatsDTCPSinkTest
{
	@Test
	public void testOne()
	{
		StatsDTCPTestSink sink = new StatsDTCPTestSink();

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

		assertThat(sink.getSentText()).isEqualTo("FormattedName:42|g");
	}
}