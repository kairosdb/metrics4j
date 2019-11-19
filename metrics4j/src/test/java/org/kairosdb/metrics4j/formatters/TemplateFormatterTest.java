package org.kairosdb.metrics4j.formatters;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.internal.ReportedMetricImpl;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class TemplateFormatterTest
{

	@Test
	void test_formatReportedMetric_noTemplate()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("this.is.my.super.special.metric");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("this.is.my.super.special.metric");
	}

	@Test
	void test_formatReportedMetric_withClassName()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("metric4j.${className}.value");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("metric4j.MyClass.value");
	}

	@Test
	void test_formatReportedMetric_startWithTemplate()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("${className}.value");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("MyClass.value");
	}

	@Test
	void test_formatReportedMetric_endWithTemplate()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("metric4j.${className}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("metric4j.MyClass");
	}

	@Test
	void test_formatReportedMetric_withTag()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("metric4j.${tag.host}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setTags(Collections.singletonMap("host", "work_machine"));
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("metric4j.work_machine");
	}

	@Test
	void test_formatReportedMetric_withMissingTag()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("metric4j.${tag.host_not}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setTags(Collections.singletonMap("host", "work_machine"));
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("metric4j.");
	}

	@Test
	void test_formatReportedMetric_comboTemplate()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("metric4j.${className}.${methodName}.${tag.host}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.setMethodName("myMethod");
		reportedMetric.setTags(Collections.singletonMap("host", "work_machine"));
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("metric4j.MyClass.myMethod.work_machine");
	}

	@Test
	void test_formatReportedMetric_fieldTemplate()
	{
		TemplateFormatter templateFormatter = new TemplateFormatter("metric4j.${className}.${methodName}.${tag.host}.${field}");
		templateFormatter.init(null);

		ReportedMetricImpl reportedMetric = new ReportedMetricImpl();
		reportedMetric.setClassName("MyClass");
		reportedMetric.setMethodName("myMethod");
		reportedMetric.setTags(Collections.singletonMap("host", "work_machine"));
		reportedMetric.addSample("value", new LongValue(1));
		String metricName = templateFormatter.formatReportedMetric(reportedMetric, reportedMetric.getSamples().get(0));

		assertThat(metricName).isEqualTo("metric4j.MyClass.myMethod.work_machine.value");
	}
}