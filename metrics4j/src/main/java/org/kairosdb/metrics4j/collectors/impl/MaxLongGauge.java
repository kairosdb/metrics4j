package org.kairosdb.metrics4j.collectors.impl;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kairosdb.metrics4j.collectors.Collector;

@ToString
@EqualsAndHashCode
public class MaxLongGauge extends LongGauge
{
	public MaxLongGauge(boolean reset)
	{
		super(reset);
	}

	public MaxLongGauge()
	{
		super(false);
	}

	@Override
	public Collector clone()
	{
		return new MaxLongGauge(reset);
	}

	@Override
	public void put(long value)
	{
		m_gauge.accumulateAndGet(value, Long::max);
	}
}
