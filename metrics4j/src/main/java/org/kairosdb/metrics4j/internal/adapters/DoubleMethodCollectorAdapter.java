package org.kairosdb.metrics4j.internal.adapters;

import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class DoubleMethodCollectorAdapter extends MethodCollectorAdapter
{
	private final Logger log = LoggerFactory.getLogger(DoubleMethodCollectorAdapter.class);

	public DoubleMethodCollectorAdapter(Object object, Method method, String fieldName)
	{
		super(object, method, fieldName);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		try
		{
			double results = (Double)m_method.invoke(m_object, null);
			metricReporter.put(m_field, new DoubleValue(results));
		}
		catch (Exception e)
		{
			log.error("Unable to collect metric from "+m_object.getClass().getName(), e);
		}
	}
}
