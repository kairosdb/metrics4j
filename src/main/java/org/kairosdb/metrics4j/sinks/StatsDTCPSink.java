package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.reporting.ReportedMetric;

import java.util.List;

public class StatsDTCPSink extends TextSocketSink
{

	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{
		//<bucket>:<value>|<type>|<sample rate>

	}
}
