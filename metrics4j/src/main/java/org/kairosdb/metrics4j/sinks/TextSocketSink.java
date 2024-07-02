package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

enum Protocol
{
	TCP, UDP
}

public abstract class TextSocketSink implements MetricSink, Closeable
{
	private static final Logger logger = LoggerFactory.getLogger(TextSocketSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();
	protected TextSocket m_textSocket;

	protected String m_host;

	protected int m_port;

	protected Protocol m_protocol = Protocol.TCP;

	protected int m_maxUdpPacketSize = 1024;

	protected int m_maxTcpBufferSize = 1024 * 6;

	protected int m_retryCount = 2;

	protected int m_retryDelay = 500; //retry delay in ms

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

	public void setRetryCount(int retryCount)
	{
		m_retryCount = retryCount;
	}

	public void setRetryDelay(int retryDelay)
	{
		m_retryDelay = retryDelay;
	}

	public void setMaxUdpPacketSize(int maxUdpPacketSize)
	{
		m_maxUdpPacketSize = maxUdpPacketSize;
	}

	public void setMaxTcpBufferSize(int maxTcpBufferSize)
	{
		m_maxTcpBufferSize = maxTcpBufferSize;
	}

	@FunctionalInterface
	public interface FlushFunction
	{
		void apply() throws IOException;
	}

	private void retry(FlushFunction flush)
	{
		int retry = 0;
		boolean success = false;

		while ((retry <= m_retryCount) && (!success))
		{
			try
			{
				flush.apply();
				success = true;
			}
			catch (IOException e)
			{
				logger.warn("Failed sending metrics to host {}", m_host);
				logger.warn("Flush exception", e);
				if (retry < m_retryCount)
				{
					try
					{
						Thread.sleep(m_retryDelay);

						m_textSocket.close();
						m_textSocket.connect();
					}
					catch (InterruptedException ex)
					{
						Thread.currentThread().interrupt();
						retry = m_retryCount; //force retry to stop
					}
					catch (IOException ioe)
					{
						logger.warn("Connection failure", ioe);
					}
				}
			}
			retry ++;
		}

		if (!success)
			logger.error("Failed sending metrics to host {}", m_host);
	}

	protected abstract class TextSocket
	{
		protected final String m_host;
		protected final int m_port;
		protected final int m_bufferSize;
		protected final ByteArrayOutputStream m_textBuffer;

		protected TextSocket(String host, int port, int bufferSize)
		{
			m_host = host;
			m_port = port;
			m_bufferSize = bufferSize;
			m_textBuffer = new ByteArrayOutputStream();
		}

		public void sendText(String msg)
		{
			logger.debug(msg);
			if (m_textBuffer.size() + msg.length() + 1 > m_bufferSize)
			{
				retry(this::flush);
			}

			try
			{
				m_textBuffer.write(msg.getBytes(StandardCharsets.UTF_8));
				m_textBuffer.write('\n');
			}
			catch (IOException e)
			{
				logger.error("This should never happen as we are writing to a byte array buffer");
			}

		}
		public abstract void flush() throws IOException;
		public abstract void connect() throws IOException;
		public abstract void close() throws IOException;
	}

	protected class TCPTextSocket extends TextSocket
	{
		private Socket m_tcpSocket;
		private OutputStream m_outputStream;

		protected TCPTextSocket(String host, int port, int maxBufferSize)
		{
			super(host, port, maxBufferSize);
		}

		@Override
		public void connect() throws IOException
		{
			m_tcpSocket = new Socket(m_host, m_port);
			m_outputStream = m_tcpSocket.getOutputStream();
		}

		@Override
		public void flush() throws IOException
		{
			byte[] buf = m_textBuffer.toByteArray();
			m_outputStream.write(buf);
			m_outputStream.flush();

			m_textBuffer.reset();
		}

		@Override
		public void close() throws IOException
		{
			m_tcpSocket.close();
		}
	}

	protected class UDPTextSocket extends TextSocket
	{
		private DatagramSocket m_udpSocket;
		private InetAddress m_udpAddress;

		protected UDPTextSocket(String host, int port, int maxPacketSize)
		{
			super(host, port, maxPacketSize);
		}

		@Override
		public void connect() throws IOException
		{
			m_udpAddress = InetAddress.getByName(m_host);
			m_udpSocket = new DatagramSocket();
		}


		@Override
		public void flush() throws IOException
		{
			byte[] buf = m_textBuffer.toByteArray();

			DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, m_udpAddress, m_port);
			m_udpSocket.send(datagramPacket);

			m_textBuffer.reset();
		}

		@Override
		public void close() throws IOException
		{
			m_udpSocket.close();
		}
	}

	protected void openSocket() throws IOException
	{
		logger.info("Connecting to {} on port {} {}", m_host, m_port, m_protocol);

		m_textSocket.connect();
	}

	protected void sendText(String msg)
	{
		logger.debug(msg);
		m_textSocket.sendText(msg);
	}


	protected void flush()
	{
		retry(m_textSocket::flush);
	}

	@Override
	public void init(MetricsContext context)
	{
		if (m_protocol == Protocol.TCP)
		{
			m_textSocket = new TCPTextSocket(m_host, m_port, m_maxTcpBufferSize);
		}
		else
		{
			m_textSocket = new UDPTextSocket(m_host, m_port, m_maxUdpPacketSize);
		}

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
			m_textSocket.close();
		}
		catch (IOException e)
		{
			logger.warn("Exception while trying to close socket", e);
		}
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}
}
