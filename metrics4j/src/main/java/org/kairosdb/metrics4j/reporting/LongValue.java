package org.kairosdb.metrics4j.reporting;

import java.util.Objects;

public class LongValue extends MetricValue
{
	private final long m_value;

	public LongValue(long value)
	{
		super(TYPE_LONG);
		m_value = value;
	}

	public long getValue()
	{
		return m_value;
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(m_value);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LongValue longValue = (LongValue) o;
		return m_value == longValue.m_value;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(m_value);
	}

	@Override
	public String toString()
	{
		return "LongValue{" +
				"value=" + m_value +
				'}';
	}
}
