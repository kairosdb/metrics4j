package org.kairosdb.metrics4j.collectors;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.ReportedMetric;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;


@XmlRootElement(name = "collector")
public class LongCounter implements LongCollector, ReportableMetric
{
	private final AtomicLong m_count = new AtomicLong(0);

	public LongCounter()
	{
		super();
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
	public void reportMetric(ReportedMetric reportedMetric)
	{
		reportedMetric.setFields(Collections.singletonMap("count", new LongValue(m_count.longValue())));
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public Collector clone()
	{
		return new LongCounter();
	}
}
