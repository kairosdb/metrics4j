package org.kairosdb.metrics4j.sinks;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ToString
public class Slf4JMetricSink implements MetricSink
{
	private static final Logger logger = LoggerFactory.getLogger(Slf4JMetricSink.class);
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();
	private static final String TRACE = "trace";
	private static final String DEBUG = "debug";
	private static final String INFO = "info";
	private static final String WARN = "warn";
	private static final String ERROR = "error";

	@Setter
	private String logLevel;

	private LogWrapper m_logWrapper;

	public Slf4JMetricSink()
	{
		logLevel = INFO;
		m_logWrapper = new InfoWrapper();
	}

	public void reportMetrics(List<FormattedMetric> metrics)
	{
		for (FormattedMetric metric : metrics)
		{
			for (FormattedMetric.Sample sample : metric.getSamples())
			{
				m_logWrapper.log("metric={}, time={}, value={}", sample.getMetricName(),
						sample.getTime(), sample.getValue().getValueAsString());
			}

		}
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}

	@Override
	public void init(MetricsContext context)
	{
		if (logLevel.toLowerCase().equals(TRACE))
			m_logWrapper = new TraceWrapper();
		else if (logLevel.toLowerCase().equals(DEBUG))
			m_logWrapper = new DebugWrapper();
		else if (logLevel.toLowerCase().equals(INFO))
			m_logWrapper = new InfoWrapper();
		else if (logLevel.toLowerCase().equals(WARN))
			m_logWrapper = new WarnWrapper();
		else if (logLevel.toLowerCase().equals(ERROR))
			m_logWrapper = new ErrorWrapper();
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
