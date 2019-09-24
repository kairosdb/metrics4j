package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "sink")
public class TelnetSink implements MetricSink, Closeable
{
	public static final String SECONDS = "SECONDS";
	public static final String MILLISECONDS = "MILLISECONDS";

	@XmlAttribute(name = "host", required = true)
	private String m_host;

	@XmlAttribute(name = "port", required = true)
	private int m_port;

	@XmlAttribute(name = "resolution", required = false)
	private String m_resolution = MILLISECONDS;

	private String m_command = "putm ";
	private Socket m_socket;
	private PrintWriter m_writer;

	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{
		for (ReportedMetric metric : metrics)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(m_command)
					.append(metric.getMetricName()).append(" ")
					.append(metric.getTime().toEpochMilli()).append(" ")
					.append(metric.getValue().getValueAsString());

			for (Map.Entry<String, String> tag : metric.getTags().entrySet())
			{
				sb.append(" ").append(tag.getKey()).append("=").append(tag.getValue());
			}

			sendText(sb.toString());
		}
	}

	private void sendText(String msg)
	{
		m_writer.println(msg);
		m_writer.flush();
	}

	private void openSocket() throws IOException
	{
		m_socket = new Socket(m_host, m_port);
		m_writer = new PrintWriter(m_socket.getOutputStream());
	}

	@Override
	public void init(MetricsContext context)
	{
		try
		{
			openSocket();

			if (m_resolution.toUpperCase().equals(SECONDS))
				m_command = "put ";
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
