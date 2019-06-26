package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.MetricValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "sink")
public class Slf4JMetricSink implements MetricSink
{
	private static Logger logger = LoggerFactory.getLogger(Slf4JMetricSink.class);
	private static String TRACE = "trace";
	private static String DEBUG = "debug";
	private static String INFO = "info";
	private static String WARN = "warn";
	private static String ERROR = "error";

	@XmlAttribute(name = "logLevel")
	private String m_logLevel;

	private LogWrapper m_logWrapper;

	public Slf4JMetricSink()
	{
		m_logLevel = INFO;
		m_logWrapper = new InfoWrapper();
	}

	public void reportMetrics(List<ReportedMetric> metrics)
	{
		for (ReportedMetric metric : metrics)
		{
			for (Map.Entry<String, MetricValue> field : metric.getFields().entrySet())
			{
				m_logWrapper.log("metric={}.{}, time={}, value={}", metric.getMetricName(),
						field.getKey(), metric.getTime(), field.getValue().getValueAsString());
			}

		}
	}

	@Override
	public void init(MetricsContext context)
	{
		if (m_logLevel.toLowerCase().equals(TRACE))
			m_logWrapper = new TraceWrapper();
		else if (m_logLevel.toLowerCase().equals(DEBUG))
			m_logWrapper = new DebugWrapper();
		else if (m_logLevel.toLowerCase().equals(INFO))
			m_logWrapper = new InfoWrapper();
		else if (m_logLevel.toLowerCase().equals(WARN))
			m_logWrapper = new WarnWrapper();
		else if (m_logLevel.toLowerCase().equals(ERROR))
			m_logWrapper = new ErrorWrapper();
	}

	public void setLogLevel(String logLevel)
	{
		m_logLevel = logLevel;
	}

	public String getLogLevel()
	{
		return m_logLevel;
	}

	@Override
	public String toString()
	{
		return "Slf4JMetricSink{" +
				"m_logLevel='" + m_logLevel + '\'' +
				'}';
	}

	private interface LogWrapper
	{
		void log(String format, Object... args);
	}

	private class TraceWrapper implements LogWrapper
	{
		@Override
		public void log(String format, Object... args)
		{
			//todo verify that passing args like this will work
			logger.trace(format, args);
		}
	}

	private class DebugWrapper implements LogWrapper
	{
		@Override
		public void log(String format, Object... args)
		{
			logger.debug(format, args);
		}
	}

	private class InfoWrapper implements LogWrapper
	{
		@Override
		public void log(String format, Object... args)
		{
			logger.info(format, args);
		}
	}

	private class WarnWrapper implements LogWrapper
	{
		@Override
		public void log(String format, Object... args)
		{
			logger.warn(format, args);
		}
	}

	private class ErrorWrapper implements LogWrapper
	{
		@Override
		public void log(String format, Object... args)
		{
			logger.error(format, args);
		}
	}

}
