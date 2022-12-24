package org.kairosdb.metrics4j.formatters;


import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class MethodToSnakeCaseTest
{
	@Test
	void test_formatReportedMetric_camelCase()
	{
		TemplateFormatter templateFormatter = new MethodToSnakeCase("metric4j.%{metricName}.%{field}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.setMethodName("myMethod");
		reportedMetric.setTags(Collections.singletonMap("host", "work_machine"));
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0), null);

		assertThat(metricName).isEqualTo("metric4j.my_method.value");
	}


	@Test
	void test_formatReportedMetric_pascalCase()
	{
		TemplateFormatter templateFormatter = new MethodToSnakeCase("metric4j.%{metricName}.%{field}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.setMethodName("MyMethod");
		reportedMetric.setTags(Collections.singletonMap("host", "work_machine"));
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0), null);

		assertThat(metricName).isEqualTo("metric4j.my_method.value");
	}


	@Test
	void test_formatReportedMetric_passMetricName()
	{
		TemplateFormatter templateFormatter = new MethodToSnakeCase("metric4j.%{metricName}.%{field}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.setMethodName("myMethod");
		reportedMetric.setTags(Collections.singletonMap("host", "work_machine"));
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0), "foo.bar");

		assertThat(metricName).isEqualTo("metric4j.foo.bar.value");
	}
}