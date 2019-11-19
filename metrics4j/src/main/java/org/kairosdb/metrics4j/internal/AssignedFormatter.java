package org.kairosdb.metrics4j.internal;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.formatters.Formatter;

@ToString
@EqualsAndHashCode
public class AssignedFormatter
{
	private final Formatter m_formatter;
	private final String m_sinkRef;

	public AssignedFormatter(Formatter formatter, String sinkRef)
	{
		m_formatter = formatter;
		m_sinkRef = sinkRef;
	}

	public Formatter getFormatter()
	{
		return m_formatter;
	}

	public String getSinkRef()
	{
		return m_sinkRef;
	}
}
