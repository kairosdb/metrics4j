package org.kairosdb.metrics4j.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Used to put an argument list (ie Object[]) as a key to a hashmap
 */
public class ArgKey
{
	private final Method m_method;
	private final Object[] m_args;

	public ArgKey(Method method, Object[] args)
	{
		m_method = method;
		m_args = args;
	}

	public Method getMethod()
	{
		return m_method;
	}

	public Object[] getArgs()
	{
		return m_args;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ArgKey argKey = (ArgKey) o;
		return Objects.equals(m_method, argKey.m_method) &&
				Arrays.equals(m_args, argKey.m_args);
	}

	@Override
	public int hashCode()
	{

		int result = Objects.hash(m_method);
		result = 31 * result + Arrays.hashCode(m_args);
		return result;
	}
}
