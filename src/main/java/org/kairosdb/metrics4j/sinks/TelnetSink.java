package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "sink")
public class TelnetSink implements MetricSink
{
	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{

	}

	@Override
	public void init(MetricsContext context)
	{

	}
}
