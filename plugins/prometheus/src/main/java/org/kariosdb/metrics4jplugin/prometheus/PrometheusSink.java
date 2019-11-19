package org.kariosdb.metrics4jplugin.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.HTTPServer;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.TriggerNotification;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.triggers.Trigger;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

@XmlRootElement(name = "sink")
public class PrometheusSink  extends CollectorRegistry implements MetricSink, Closeable, TriggerNotification
{
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter("_");
	private HTTPServer m_httpServer;
	private PrometheusTrigger m_trigger;
	private List<ReportedMetric> m_reportedMetrics;

	@XmlAttribute(name = "port", required = true)
	private int m_serverPort;

	@Override
	public void reportMetrics(List<ReportedMetric> metrics)
	{
		m_reportedMetrics = metrics;
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}

	@Override
	public void init(MetricsContext context)
	{
		context.registerTriggerNotification(this);

		try
		{
			m_httpServer = new HTTPServer(new InetSocketAddress(m_serverPort), this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void close() throws IOException
	{
		m_httpServer.stop();
	}

	@Override
	public void newTrigger(String name, Trigger trigger)
	{
		if (trigger instanceof PrometheusTrigger)
		{
			m_trigger = (PrometheusTrigger)trigger;
		}
	}

	@Override
	public Enumeration<Collector.MetricFamilySamples> metricFamilySamples()
	{
		m_trigger.reportMetrics();
		Vector<Collector.MetricFamilySamples> ret = new Vector<>();

		for (ReportedMetric reportedMetric : m_reportedMetrics)
		{
			List<Collector.MetricFamilySamples.Sample> promSamples = new ArrayList<>();
			List<String> keys = new ArrayList<>();
			List<String> values = new ArrayList<>();

			for (Map.Entry<String, String> tagEntry : reportedMetric.getTags().entrySet())
			{
				keys.add(tagEntry.getKey());
				values.add(tagEntry.getValue());
			}

			//convert to MetricFamilySamples
			for (ReportedMetric.Sample sample : reportedMetric.getSamples())
			{
				if (sample.getValue() instanceof DoubleValue)
				{
					promSamples.add(new Collector.MetricFamilySamples.Sample(sample.getMetricName(),
							keys, values, ((DoubleValue) sample.getValue()).getValue(), sample.getTime().toEpochMilli()));
				}
				else if (sample.getValue() instanceof LongValue)
				{
					promSamples.add(new Collector.MetricFamilySamples.Sample(sample.getMetricName(),
							keys, values, ((LongValue) sample.getValue()).getValue(), sample.getTime().toEpochMilli()));
				}

			}

			//todo figure out type and pass help
			ret.add(new Collector.MetricFamilySamples(reportedMetric.getMetricName(),
					Collector.Type.COUNTER, "help", promSamples));

		}

		return ret.elements();
	}

	@Override
	public Enumeration<Collector.MetricFamilySamples> filteredMetricFamilySamples(Set<String> includedNames)
	{
		return null;
	}
}
