package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.triggers.MetricCollection;
import org.kairosdb.metrics4j.triggers.Trigger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 build this class and then add it to the trigger
 For a specific trigger this class holds all the sinks and collectors that will
 be scraped by this trigger.
 */
public class TriggerMetricCollection implements MetricCollection
{
	private final Trigger m_trigger;
	private List<CollectorContainer> m_collectors;
	private Set<SinkQueue> m_sinkQueues; //we use this to flush the sinks when we are done.

	public TriggerMetricCollection(Trigger trigger)
	{
		m_trigger = trigger;
		m_trigger.setMetricCollection(this);
		m_collectors = new ArrayList<>();
		m_sinkQueues = new HashSet<>();
	}

	public Trigger getTrigger()
	{
		return m_trigger;
	}

	public void addCollector(CollectorContainer collector)
	{
		m_collectors.add(collector);

		m_sinkQueues.addAll(collector.getSinkQueueList());
	}

	@Override
	public void reportMetrics(Instant now)
	{
		for (CollectorContainer collector : m_collectors)
		{
			//maybe just pass a timestamp into this
			collector.reportMetrics(now);
		}

		//Flush out the queues to all sinks
		for (SinkQueue sinkQueue : m_sinkQueues)
		{
			sinkQueue.flush();
		}
	}
}
