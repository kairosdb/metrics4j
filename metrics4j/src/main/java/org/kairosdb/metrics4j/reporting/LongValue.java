package org.kairosdb.metrics4j.reporting;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Objects;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
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

}
