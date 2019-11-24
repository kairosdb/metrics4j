package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.atomic.AtomicLong;

@XmlRootElement(name = "collector")
public class LongGauge implements LongCollector
{
	private final AtomicLong m_gauge = new AtomicLong(0);

	@XmlAttribute(name = "reset")
	private boolean m_reset = false;

	public LongGauge(boolean reset)
	{
		super();
		m_reset = reset;
	}

	public LongGauge()
	{
		this(false);
	}


	@Override
	public void put(long value)
	{
		m_gauge.set(value);
	}

	@Override
	public Collector clone()
	{
		return new LongGauge(m_reset);
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;

		if (m_reset)
			value = m_gauge.getAndSet(0);
		else
			value = m_gauge.get();

		metricReporter.put("gauge", new LongValue(value));
	}

}
