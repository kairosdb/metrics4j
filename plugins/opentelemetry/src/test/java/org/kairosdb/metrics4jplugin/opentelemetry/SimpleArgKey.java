package org.kairosdb.metrics4jplugin.opentelemetry;

import org.kairosdb.metrics4j.internal.ArgKey;

import java.util.List;

public class SimpleArgKey implements ArgKey
{
	private final List<String> m_configPath;
	private final String m_methodName;
	private final String m_className;

	public SimpleArgKey(List<String> configPath, String methodName, String className)
	{
		m_configPath = configPath;
		m_methodName = methodName;
		m_className = className;
	}

	@Override
	public List<String> getConfigPath()
	{
		return m_configPath;
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
}
