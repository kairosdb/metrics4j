package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

enum Protocol
{
	TCP, UDP
}

public abstract class TextSocketSink implements MetricSink, Closeable
{
	private static final Logger logger = LoggerFactory.getLogger(TextSocketSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();
	protected Socket m_tcpSocket;
	protected PrintWriter m_writer;
	protected DatagramSocket m_udpSocket;
	protected ByteArrayOutputStream m_udpBuffer;
	protected InetAddress m_udpAddress;
	protected int m_packetSize;

	protected String m_host;

	protected int m_port;

	protected Protocol m_protocol = Protocol.TCP;

	protected int m_maxUdpPacketSize = 1024;

	public void setHost(String host)
	{
		m_host = host;
	}

	public void setPort(int port)
	{
		m_port = port;
	}

	public void setProtocol(Protocol protocol)
	{
		m_protocol = protocol;
	}

	public void setMaxUdpPacketSize(int maxUdpPacketSize)
	{
		m_maxUdpPacketSize = maxUdpPacketSize;
	}

	protected void openSocket() throws IOException
	{
		logger.info("Connecting to {} on port {} {}", m_host, m_port, m_protocol);

		if (m_protocol == Protocol.TCP)
		{
			m_tcpSocket = new Socket(m_host, m_port);
			m_writer = new PrintWriter(m_tcpSocket.getOutputStream());
		}
		else
		{
			m_udpSocket = new DatagramSocket();
			m_udpAddress = InetAddress.getByName(m_host);
			m_udpBuffer = new ByteArrayOutputStream();
			m_writer = new PrintWriter(m_udpBuffer);
		}
	}

	protected void sendText(String msg)
	{
		try
		{
			logger.debug(msg);
			if (m_protocol == Protocol.UDP && (m_packetSize + msg.length() > m_maxUdpPacketSize))
			{
				flushUdp();
			}

			m_packetSize = msg.length() + 1; //add 1 for \n
			m_writer.println(msg);
		}
		catch (Exception e)
		{
			logger.error("Failed sending metrics to host {}", m_host);
		}
	}

	private void flushUdp() throws IOException
	{
		m_writer.flush();
		byte[] buf = m_udpBuffer.toByteArray();
		m_udpBuffer.reset();
		m_packetSize = 0;
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, m_udpAddress, m_port);
		m_udpSocket.send(datagramPacket);
	}

	protected void flush()
	{
		try
		{
			if (m_protocol == Protocol.UDP)
				flushUdp();
			else
				m_writer.flush();
		}
		catch (Exception e)
		{
			logger.error("Failed sending metrics to host {}", m_host);
		}
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
			if (m_tcpSocket != null)
				m_tcpSocket.close();

			if (m_udpSocket != null)
				m_udpSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}
}
