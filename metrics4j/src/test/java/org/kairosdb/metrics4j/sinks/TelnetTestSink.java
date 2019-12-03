package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;

public class TelnetTestSink extends TelnetSink
{
	private String m_sentText;

	public TelnetTestSink(Resolution resolution)
	{
		super(resolution);
	}

	@Override
	public void init(MetricsContext context)
	{
		super.init(context);
	}

	@Override
	public void close()
	{
	}

	@Override
	protected void flush()
	{
	}

	@Override
	protected void sendText(String msg)
	{
		m_sentText = msg;
	}

	public String getSentText()
	{
		return m_sentText;
	}
}
