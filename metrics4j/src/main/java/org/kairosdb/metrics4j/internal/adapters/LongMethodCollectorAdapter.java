package org.kairosdb.metrics4j.internal.adapters;

import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

public class LongMethodCollectorAdapter extends MethodCollectorAdapter
{
	private final Logger log = LoggerFactory.getLogger(LongMethodCollectorAdapter.class);

	public LongMethodCollectorAdapter(Object object, Method method, String fieldName)
	{
		super(object, method, fieldName);
	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		try
		{
			long results = (Long)m_method.invoke(m_object, null);
			metricReporter.put(m_field, new LongValue(results));
		}
		catch (Exception e)
		{
			log.error("Unable to collect metric from "+m_object.getClass().getName(), e);
		}
	}


}
