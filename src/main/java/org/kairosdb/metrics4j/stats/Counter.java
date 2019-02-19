package org.kairosdb.metrics4j.stats;

import org.kairosdb.metrics4j.reporter.LongValue;
import org.kairosdb.metrics4j.reporter.ReportedMetric;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

public class Counter implements ReportableStats
{
	private final AtomicLong m_count = new AtomicLong(0);


	public void add(long count)
	{
		m_count.addAndGet(count);
	}

	public Counter reset()
	{
		m_count.set(0);
		return this;
	}


	@Override
	public void reportMetrics(ReportedMetric reportedMetric)
	{
		reportedMetric.setFields(Collections.singletonMap("count", new LongValue(m_count.longValue())));
	}
}
