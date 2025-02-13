package org.kairosdb.metrics.metrics4jplugin.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.kairosdb.metrics4j.MetricSourceManager;

import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.INFO;
import static ch.qos.logback.classic.Level.TRACE;
import static ch.qos.logback.classic.Level.WARN;


/**
 Simple appender for use with logback applications.  Add this appender to your
 application logback.xml file, and it will send counts of log entries to your
 favorite metrics system.
 */
public class Metrics4jAppender extends AppenderBase<ILoggingEvent>
{
	private static final LoggerStats stats = MetricSourceManager.getSource(LoggerStats.class);


	@Override
	protected void append(ILoggingEvent event)
	{
		String logger = event.getLoggerName();
		switch (event.getLevel().toInt()) {
			case Level.TRACE_INT:
				stats.logCount(name, TRACE.levelStr, logger).put(1);
				stats.trace(name, logger).put(1);
				break;
			case Level.DEBUG_INT:
				stats.logCount(name, DEBUG.levelStr, logger).put(1);
				stats.debug(name, logger).put(1);
				break;
			case Level.INFO_INT:
				stats.logCount(name, INFO.levelStr, logger).put(1);
				stats.info(name, logger).put(1);
				break;
			case Level.WARN_INT:
				stats.logCount(name, WARN.levelStr, logger).put(1);
				stats.warn(name, logger).put(1);
				break;
			case Level.ERROR_INT:
				stats.logCount(name, ERROR.levelStr, logger).put(1);
				stats.error(name, logger).put(1);
				break;
			default:
				break;
		}
	}


}
