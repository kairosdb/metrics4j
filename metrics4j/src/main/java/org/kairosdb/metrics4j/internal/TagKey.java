package org.kairosdb.metrics4j.internal;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 Contains tags and can be used as a key for a map
 */
public class TagKey
{
	private final Map<String, String> m_tags;
	private final String m_key;


	private TagKey(Map<String, String> tags, String key)
	{
		m_tags = Collections.unmodifiableMap(tags);
		m_key = key;
	}

	public Map<String, String> getTags()
	{
		return m_tags;
	}


	public static Builder newBuilder()
	{
		return new Builder();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TagKey tagKey = (TagKey) o;
		return m_key.equals(tagKey.m_key);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(m_key);
	}

	@Override
	public String toString()
	{
		return "TagKey{" +
				"m_key='" + m_key + '\'' +
				'}';
	}

	public static class Builder
	{
		private final Map<String, String> m_tags;
		private final StringBuilder m_keyBuilder;

		private Builder()
		{
			m_tags = new HashMap<>();
			m_keyBuilder = new StringBuilder();
		}

		public Builder addTag(String key, String value)
		{
			m_tags.put(key, value);
			m_keyBuilder.append(key).append(value);
			return this;
		}

		public TagKey build()
		{
			return new TagKey(m_tags, m_keyBuilder.toString());
		}
	}
}
