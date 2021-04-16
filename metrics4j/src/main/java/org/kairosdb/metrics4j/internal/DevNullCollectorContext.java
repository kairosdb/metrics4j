package org.kairosdb.metrics4j.internal;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DevNullCollectorContext implements CollectorContext
{
	private static CollectorCollection COLLECTION = new DevNullCollectorCollection();

	@Override
	public CollectorCollection getCollection()
	{
		return COLLECTION;
	}

	@Override
	public Map<String, String> getTags()
	{
		return Collections.emptyMap();
	}

	@Override
	public List<SinkQueue> getSinkQueueList()
	{
		return Collections.emptyList();
	}

	@Override
	public void reportMetrics(Instant now)
	{

	}
}
