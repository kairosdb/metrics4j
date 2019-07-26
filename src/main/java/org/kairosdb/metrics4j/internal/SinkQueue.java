package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;

import java.util.ArrayList;
import java.util.List;

//MUST BE THREAD SAFE
public class SinkQueue
{
	private final MetricSink m_sink;
	private List<ReportedMetric> m_metricList;
	private Object m_queueLock = new Object();

	public SinkQueue(MetricSink sink)
	{
		m_sink = sink;
		m_metricList = new ArrayList<>();
	}

	public void flush()
	{
		synchronized (m_queueLock)
		{
			List<ReportedMetric> metrics = m_metricList;
			m_metricList = new ArrayList<>();

			m_sink.reportMetrics(metrics);
		}
	}

	public MetricSink getSink()
	{
		return m_sink;
	}

	public void addMetric(ReportedMetric reportedMetric)
	{
		synchronized (m_queueLock)
		{
			m_metricList.add(reportedMetric);
		}
	}
}
