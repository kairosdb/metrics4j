package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;

public class StatsDTCPTestSink extends StatsDTCPSink
{
	private String m_sentText;

	public StatsDTCPTestSink()
	{
		super();
	}

	@Override
	public void init(MetricsContext context)
	{
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
