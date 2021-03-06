package org.kairosdb.metrics4j.reporting;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DoubleValue extends MetricValue
{
	private final double m_value;

	public DoubleValue(double value)
	{
		super(TYPE_DOUBLE);
		m_value = value;
	}

	public double getValue()
	{
		return m_value;
	}

	@Override
	public String getValueAsString()
	{
		return String.valueOf(m_value);
	}

}
