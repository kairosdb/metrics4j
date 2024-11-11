package org.kairosdb.metrics4j.internal;

import lombok.ToString;
import org.kairosdb.metrics4j.collectors.MetricCollector;

import java.util.ArrayList;
import java.util.List;

import static org.kairosdb.metrics4j.configuration.MetricConfig.PATH_SPLITTER_REGEX;

@ToString
public class CustomArgKey implements ArgKey
{
	private final MetricCollector m_collector;
	private final String m_className;
	private final String m_simpleClassName;

	public CustomArgKey(MetricCollector collector)
	{
		m_collector = collector;
		m_className = m_collector.getClass().getDeclaringClass().getName();
		m_simpleClassName = m_collector.getClass().getDeclaringClass().getSimpleName();
	}


	@Override
	public List<String> getConfigPath()
	{
		List<String> ret = new ArrayList<>();
		String[] split = m_collector.getClass().getDeclaringClass().getName().split(PATH_SPLITTER_REGEX);
		for (String s : split)
		{
			ret.add(s);
		}

		return ret;
	}


	@Override
	public String getMethodName()
	{
		return "";
	}

	@Override
	public String getClassName()
	{
		return m_className;
	}

	@Override
	public String getSimpleClassName() {
		return m_simpleClassName;
	}
}
