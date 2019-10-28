package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.concurrent.atomic.AtomicLong;


@XmlRootElement(name = "collector")
public class LongCounter implements LongCollector, MetricCollector
{
	private final AtomicLong m_count = new AtomicLong(0);

	@XmlAttribute(name = "reset")
	private boolean m_reset = false;

	public LongCounter(boolean reset)
	{
		super();
		m_reset = reset;
	}

	public LongCounter()
	{
		this(false);
	}

	/*@XmlElement
	public void setBob(String bob)
	{
		System.out.println("Bob is "+bob);
	}*/

	public void put(long count)
	{
		m_count.addAndGet(count);
	}

	public Collector reset()
	{
		m_count.set(0);
		return this;
	}


	@Override
	public void reportMetric(MetricReporter metricReporter)
	{
		long value;

		if (m_reset)
			value = m_count.getAndSet(0);
		else
			value = m_count.longValue();

		metricReporter.put("count", new LongValue(value));
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public Collector clone()
	{
		return new LongCounter(m_reset);
	}
}
