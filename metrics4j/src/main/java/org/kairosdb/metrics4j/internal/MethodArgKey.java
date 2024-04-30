package org.kairosdb.metrics4j.internal;

import lombok.ToString;
import org.kairosdb.metrics4j.MetricThreadHelper;
import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.configuration.ImplementationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.kairosdb.metrics4j.configuration.MetricConfig.PATH_SPLITTER_REGEX;

public class MethodArgKey implements ArgKey
{
	private final Method m_method;
	private final Object[] m_args;
	private final List<String> m_configPath;

	public MethodArgKey(Method method, Object[] args)
	{
		m_method = method;
		m_args = args;

		m_configPath = new ArrayList<>();
		String[] split = m_method.getDeclaringClass().getName().split(PATH_SPLITTER_REGEX);
		for (String s : split)
		{
			m_configPath.add(s);
		}

		m_configPath.add(m_method.getName());
	}

	public List<String> getConfigPath()
	{
		return m_configPath;
	}

	public Method getMethod()
	{
		return m_method;
	}


	public TagKey getTagKey(Map<String, String> overrides)
	{
		Iterator<Map.Entry<String, String>> threadTagIterator = MetricThreadHelper.getTagIterator();

		if ((m_args == null || m_args.length == 0) && (!threadTagIterator.hasNext()))
			return TagKey.newBuilder().build();
		else
		{
			TagKey.Builder builder = TagKey.newBuilder();
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
						break;
					}
				}

				if (tagKey == null)
					throw new ImplementationException("All parameters on "+m_method.getDeclaringClass().getName()+"."+m_method.getName()+" must be annotated with @Key()");

				/*
				If there is an override configured for a tag that is a parameter we must
				set it in the tag key.  This will make sure that the same collector is
				returned and the data is aggregated properly.
				 */
				String override = overrides.get(tagKey);

				if (override != null)
					builder.addTag(tagKey, override);
				else
				{
					if (m_args[i] == null)
						builder.addTag(tagKey, "null");
					else
						builder.addTag(tagKey, m_args[i].toString());
				}
			}

			//Add any tags that are set on the thread.
			while (threadTagIterator.hasNext())
			{
				Map.Entry<String, String> tagEntry = threadTagIterator.next();
				String key = tagEntry.getKey();
				String override = overrides.get(key);

				if (override != null)
					builder.addTag(key, override);
				else
					builder.addTag(key, tagEntry.getValue());
			}

			TagKey tag = builder.build();

			return tag;
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

	/**
	 We intentionally only use the config path for equals and hash
	 @param o Object to compare to
	 @return If the objects are equal
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MethodArgKey that = (MethodArgKey) o;
		return m_configPath.equals(that.m_configPath);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(m_configPath);
	}
}
