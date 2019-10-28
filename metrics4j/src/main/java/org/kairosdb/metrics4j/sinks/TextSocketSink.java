package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
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
	TCP, UDP;
}

public abstract class TextSocketSink implements MetricSink, Closeable
{
	private static final Logger logger = LoggerFactory.getLogger(TextSocketSink.class);
	protected Socket m_tcpSocket;
	protected PrintWriter m_writer;
	protected DatagramSocket m_udpSocket;
	protected ByteArrayOutputStream m_udpBuffer;
	protected InetAddress m_udpAddress;
	protected int m_packetSize;

	@XmlAttribute(name = "host", required = true)
	protected String m_host;

	@XmlAttribute(name = "port", required = true)
	protected int m_port;

	@XmlAttribute(name = "protocol", required = false)
	protected Protocol m_protocol = Protocol.TCP;

	@XmlAttribute(name = "max_udp_packet_size", required = false)
	protected int m_maxUdpPacketSize = 1024;


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
}
