package org.kairosdb.metrics4j.internal.adapters;

import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.kairosdb.metrics4j.reporting.StringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class StringMethodCollectorAdapter extends MethodCollectorAdapter
{
	private final Logger log = LoggerFactory.getLogger(StringMethodCollectorAdapter.class);

	public StringMethodCollectorAdapter(Object object, Method method, String fieldName)
	{
		super(object, method, fieldName);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		try
		{
			CharSequence results = (CharSequence)m_method.invoke(m_object, null);
			metricReporter.put(m_field, new StringValue(results));
		}
		catch (Exception e)
		{
			log.error("Unable to collect metric from "+m_object.getClass().getName(), e);
		}
	}

}
