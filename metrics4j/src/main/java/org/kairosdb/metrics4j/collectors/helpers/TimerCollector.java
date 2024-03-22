package org.kairosdb.metrics4j.collectors.helpers;

import lombok.Setter;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.internal.DoubleTimeReporter;
import org.kairosdb.metrics4j.internal.LongTimeReporter;
import org.kairosdb.metrics4j.internal.TimeReporter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_DELTA_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.AGGREGATION_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.CHRONO_UNIT_KEY;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_COUNTER_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_GAUGE_VALUE;
import static org.kairosdb.metrics4j.internal.ReportingContext.TYPE_KEY;


public abstract class TimerCollector implements DurationCollector
{
	public static enum ReportFormat
	{
		LONG,
		DOUBLE
	}
	private final Ticker m_ticker = new SystemTicker();
	private ChronoUnit m_reportUnit = ChronoUnit.MILLIS;
	private ReportFormat m_reportFormat = ReportFormat.LONG;
	protected TimeReporter m_timeReporter = new LongTimeReporter(ChronoUnit.MILLIS);
	protected Map<String, String> m_reportContext = new HashMap<>();

	private void updateTimeReporter()
	{
		if (m_reportFormat == ReportFormat.LONG)
		{
			m_timeReporter = new LongTimeReporter(m_reportUnit);
		}
		else
		{
			m_timeReporter = new DoubleTimeReporter(m_reportUnit);
		}

	}

	public TimerCollector()
	{
		m_reportContext.put(CHRONO_UNIT_KEY, ChronoUnit.MILLIS.name());
	}

	/**
	 Unit to report metric as.  Supported units are NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, DAYS
	 */
	public void setReportUnit(ChronoUnit reportUnit)
	{
		m_reportContext.put(CHRONO_UNIT_KEY, reportUnit.name());
		m_reportUnit = reportUnit;
		updateTimeReporter();
	}

	public ChronoUnit getReportUnit()
	{
		return m_reportUnit;
	}

	/**
	 Report format is either LONG or DOUBLE.  Double truncates at 3 decimal places
	 * @param reportFormat
	 */
	public void setReportFormat(ReportFormat reportFormat)
	{
		m_reportFormat = reportFormat;
		updateTimeReporter();
	}

	public ReportFormat getReportFormat()
	{
		return m_reportFormat;
	}

	@Override
	public <T> T timeEx(Callable<T> callable) throws Exception
	{
		try (BlockTimer ignored = time())
		{
			return callable.call();
		}
	}

	@Override
	public <T> T time(TimeCallable<T> callable)
	{
		try (BlockTimer ignored = time())
		{
			return callable.call();
		}
	}

	@Override
	public BlockTimer time()
	{
		return new BlockTimer(this, m_ticker);
	}

	public abstract Collector clone();

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{
		String reportUnit = contextProperties.get("report-unit");

		if (reportUnit != null)
		{
			setReportUnit(ChronoUnit.valueOf(reportUnit));
		}

		String reportFormat = contextProperties.get("report-format");

		if (reportFormat != null)
		{
			setReportFormat(ReportFormat.valueOf(reportFormat));
		}
	}
}
