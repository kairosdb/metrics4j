package org.kairosdb.metrics4j.internal;

import java.util.ArrayList;
import java.util.List;

public class LambdaArgKey implements ArgKey
{
	private final String m_className;
	private final String m_methodName;

	public LambdaArgKey(String className, String methodName)
	{
		m_className = className;
		m_methodName = methodName;
	}

	@Override
	public List<String> getConfigPath()
	{
		List<String> ret = new ArrayList<>();
		String[] split = m_className.split("\\.");
		for (String s : split)
		{
			ret.add(s);
		}

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
}
