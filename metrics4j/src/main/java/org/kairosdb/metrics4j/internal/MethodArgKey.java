package org.kairosdb.metrics4j.internal;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.configuration.ConfigurationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ToString
@EqualsAndHashCode
public class MethodArgKey implements ArgKey
{
	private final Method m_method;
	private final Object[] m_args;

	public MethodArgKey(Method method, Object[] args)
	{
		m_method = method;
		m_args = args;
	}

	public List<String> getConfigPath()
	{
		List<String> ret = new ArrayList<>();
		String[] split = m_method.getDeclaringClass().getName().split("\\.");
		for (String s : split)
		{
			ret.add(s);
		}

		ret.add(m_method.getName());

		return ret;
	}

	public Method getMethod()
	{
		return m_method;
	}

	public Object[] getArgs()
	{
		return m_args;
	}

	public Map<String, String> getTags()
	{

		if (m_args == null || m_args.length == 0)
			return new HashMap<>();
		else
		{
			Map<String, String> ret = new HashMap<>();
			Annotation[][] parameterAnnotations = m_method.getParameterAnnotations();
			for (int i = 0; i < parameterAnnotations.length; i ++)
			{
				String tagKey = null;

				Annotation[] annotations = parameterAnnotations[i];
				for (Annotation annotation : annotations)
				{
					if (annotation instanceof Key)
					{
						Key key = (Key)annotation;
						tagKey = key.value();
					}
				}

				if (tagKey == null)
					throw new ConfigurationException("Parameter "+m_method.getParameters()[i].getName()+" on method "+m_method.getName()+" has not been annotated with @Key()");

				ret.put(tagKey, (String)m_args[i]);
			}

			return ret;
		}
	}

	@Override
	public String getMethodName()
	{
		return m_method.getName();
	}

	@Override
	public String getClassName()
	{
		return m_method.getDeclaringClass().getName();
	}

	@Override
	public String toString()
	{
		return m_method.getDeclaringClass().getName() + "." + m_method.getName();
	}
}
