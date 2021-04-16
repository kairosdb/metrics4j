package org.kairosdb.metrics4j.internal;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 This class is only used when metrics4j is used within a unit test.
 */
public class TestingCollectorContext implements CollectorContext
{
	private final CollectorCollection m_collection;

	public TestingCollectorContext(CollectorCollection collection)
	{
		m_collection = collection;
	}

	@Override
	public CollectorCollection getCollection()
	{
		return m_collection;
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
