package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;

import java.util.ArrayList;
import java.util.List;

public class CustomArgKey implements ArgKey
{
	private final MetricCollector m_collector;

	public CustomArgKey(MetricCollector collector)
	{
		m_collector = collector;
	}


	@Override
	public List<String> getConfigPath()
	{
		List<String> ret = new ArrayList<>();
		String[] split = m_collector.getClass().getDeclaringClass().getName().split("\\.");
		for (String s : split)
		{
			ret.add(s);
		}

		return ret;
	}


	@Override
	public String getMethodName()
	{
		return null;
	}


	@Override
	public String getClassName()
	{
		return m_collector.getClass().getDeclaringClass().getName();
	}
}
