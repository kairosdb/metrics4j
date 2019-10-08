package org.kairosdb.metrics4j.triggers;

import org.kairosdb.metrics4j.MetricsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@XmlRootElement(name = "trigger")
public class IntervalTrigger implements Trigger, Runnable
{
	private static Logger log = LoggerFactory.getLogger(IntervalTrigger.class);

	private MetricCollection m_collection;

	@XmlAttribute(name = "interval")
	private long m_delay;

	@XmlAttribute(name = "unit")
	private TimeUnit m_unit;

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
				t.setDaemon(true);
				return t;
			}
		}).scheduleAtFixedRate(this, m_delay, m_delay, m_unit);
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
