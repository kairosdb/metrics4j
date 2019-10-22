package org.kairosdb.metrics4j.internal;

import java.util.ArrayList;
import java.util.List;

public class LambdaArgKey implements ArgKey
{
	private final String m_className;

	public LambdaArgKey(String className)
	{
		m_className = className;
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
		return null;
	}

	@Override
	public String getClassName()
	{
		return m_className;
	}
}
