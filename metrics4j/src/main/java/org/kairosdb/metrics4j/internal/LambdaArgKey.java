package org.kairosdb.metrics4j.internal;

import lombok.ToString;
import org.kairosdb.metrics4j.util.NameParser;

import java.util.ArrayList;
import java.util.List;

import static org.kairosdb.metrics4j.configuration.MetricConfig.PATH_SPLITTER_REGEX;

@ToString
public class LambdaArgKey implements ArgKey
{
	private final String m_className;
	private final String m_methodName;
	private String m_simpleClassName;

	public LambdaArgKey(String className, String methodName)
	{
		m_className = className;
		m_methodName = methodName;
		m_simpleClassName = NameParser.parseSimpleClassName(className);
	}

	@Override
	public List<String> getConfigPath()
	{
		List<String> ret = new ArrayList<>();
		String[] split = m_className.split(PATH_SPLITTER_REGEX);
		for (String s : split)
		{
			ret.add(s);
		}

		if (m_methodName != null)
			ret.add(m_methodName);

		return ret;
	}

	@Override
	public String getMethodName()
	{
		return m_methodName;
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
