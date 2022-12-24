package org.kairosdb.metrics4j.internal.adapters;

import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class DurationMethodCollectorAdapter extends MethodCollectorAdapter
{
	private final Logger log = LoggerFactory.getLogger(DurationMethodCollectorAdapter.class);
	private ChronoUnit m_reportUnit = ChronoUnit.MILLIS;

	public DurationMethodCollectorAdapter(Object object, Method method, String fieldName)
	{
		super(object, method, fieldName);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		try
		{
			Duration results = (Duration)m_method.invoke(m_object, null);
			metricReporter.put(m_field, new LongValue(TimerCollector.getValue(m_reportUnit, results)));
		}
		catch (Exception e)
		{
			log.error("Unable to collect metric from "+m_object.getClass().getName(), e);
		}
	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{
		super.setContextProperties(contextProperties);
		String reportUnit = contextProperties.get("report-unit");

		if (reportUnit != null)
		{
			m_reportUnit = ChronoUnit.valueOf(reportUnit);
		}
	}
}
