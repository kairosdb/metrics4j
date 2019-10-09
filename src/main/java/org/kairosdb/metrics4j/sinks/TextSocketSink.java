package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class TextSocketSink implements MetricSink, Closeable
{
	private static final Logger logger = LoggerFactory.getLogger(TextSocketSink.class);
	protected Socket m_socket;
	protected PrintWriter m_writer;

	@XmlAttribute(name = "host", required = true)
	protected String m_host;

	@XmlAttribute(name = "port", required = true)
	protected int m_port;

	protected void openSocket() throws IOException
	{
		logger.info("Connecting to {} on port {}", m_host, m_port);
		m_socket = new Socket(m_host, m_port);
		m_writer = new PrintWriter(m_socket.getOutputStream());
	}

	protected void sendText(String msg)
	{
		logger.debug(msg);
		m_writer.println(msg);
		m_writer.flush();
	}

	@Override
	public void init(MetricsContext context)
	{
		try
		{
			openSocket();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void close()
	{
		try
		{
			m_socket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
