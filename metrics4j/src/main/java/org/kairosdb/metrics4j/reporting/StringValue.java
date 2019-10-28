package org.kairosdb.metrics4j.reporting;

import java.util.Objects;

public class StringValue extends MetricValue
{
	private final String m_value;

	public StringValue(String value)
	{
		super(TYPE_STRING);
		m_value = value;
	}

	@Override
	public String getValueAsString()
	{
		return m_value;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		StringValue that = (StringValue) o;
		return Objects.equals(m_value, that.m_value);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(m_value);
	}

	@Override
	public String toString()
	{
		return "StringValue{" +
				"value='" + m_value + '\'' +
				'}';
	}
}
