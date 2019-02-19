package org.kairosdb.metrics4j.reporter;

public class DoubleValue extends MetricValue
{
	private final double m_value;

	protected DoubleValue(double value)
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
