package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;

import java.util.ArrayList;
import java.util.List;

//MUST BE THREAD SAFE
public class SinkQueue
{
	private final MetricSink m_sink;
	private final String m_sinkName;
	private List<FormattedMetric> m_metricList;
	private final Object m_queueLock = new Object();

	public SinkQueue(MetricSink sink, String sinkName)
	{
		m_sink = sink;
		m_sinkName = sinkName;
		m_metricList = new ArrayList<>();
	}

	public void flush()
	{
		synchronized (m_queueLock)
		{
			List<FormattedMetric> metrics = m_metricList;
			m_metricList = new ArrayList<>();

			m_sink.reportMetrics(metrics);
		}
	}

	public MetricSink getSink()
	{
		return m_sink;
	}

	public String getSinkName()
	{
		return m_sinkName;
	}

	public void addMetric(FormattedMetric reportedMetric)
	{
		synchronized (m_queueLock)
		{
			m_metricList.add(reportedMetric);
		}
	}
}
