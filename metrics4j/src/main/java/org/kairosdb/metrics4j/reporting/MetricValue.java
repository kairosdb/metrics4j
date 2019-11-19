package org.kairosdb.metrics4j.reporting;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class MetricValue
{
	public static final String TYPE_LONG = "long";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_STRING = "string";

	private final String m_type;

	public MetricValue(String type)
	{
		m_type = type;
	}

	public String getType()
	{
		return m_type;
	}

	public abstract String getValueAsString();
}
