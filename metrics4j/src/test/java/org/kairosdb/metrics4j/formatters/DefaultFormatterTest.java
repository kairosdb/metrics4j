package org.kairosdb.metrics4j.formatters;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DefaultFormatterTest
{
	@Test
	public void testDefaultFormat()
	{
		DefaultFormatter formatter = new DefaultFormatter("-");

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl()
				.setClassName("AwesomeClass")
				.setMethodName("testMethod")
				.addSample("value", new LongValue(1L)).reportedMetric();

		String formattedMetric = formatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0), null);

		assertThat(formattedMetric).isEqualTo("AwesomeClass-testMethod-value");
	}

	@Test
	public void testMetricNameFormat()
	{
		DefaultFormatter formatter = new DefaultFormatter("-");

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl()
				.setClassName("AwesomeClass")
				.setMethodName("testMethod")
				.addSample("value", new LongValue(1L)).reportedMetric();

		String formattedMetric = formatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0), "BilboBaggins");

		assertThat(formattedMetric).isEqualTo("BilboBaggins-value");
	}

}