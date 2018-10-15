package org.kairosdb.metrics4j.stats;

import java.util.concurrent.atomic.AtomicLong;

public class Counter
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


}
