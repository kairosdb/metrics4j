package org.kairosdb.metrics4j.triggers;

import lombok.Getter;
import lombok.Setter;
import org.kairosdb.metrics4j.MetricsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;


public class IntervalTrigger implements Trigger, Runnable
{
	private static Logger log = LoggerFactory.getLogger(IntervalTrigger.class);

	private MetricCollection m_collection;

	@Setter
	private Duration interval;


	@Override
	public void setMetricCollection(MetricCollection collection)
	{
		m_collection = collection;
	}

	@Override
	public void init(MetricsContext context)
	{
		Executors.newSingleThreadScheduledExecutor(new ThreadFactory()
		{
			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = Executors.defaultThreadFactory().newThread(r);
				t.setName("metrics4j IntervalTrigger");
				t.setDaemon(true);
				return t;
			}
		}).scheduleAtFixedRate(this, interval.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public void run()
	{
		try
		{
			log.debug("Trigger collecting metrics");
			m_collection.reportMetrics(Instant.now());
		}
		catch (Throwable e)
		{
			log.error("Error while trying to send metrics", e);
		}
	}
}
