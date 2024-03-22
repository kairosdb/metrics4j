package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.collectors.DurationCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;

public interface TestMetricSource
{
	enum TaskType
	{
		UPLOAD,
		DOWNLOAD;

		@Override public String toString()
		{
			return name().toLowerCase();
		}
	}
	LongCollector reportSize(@Key("host") String host);

	LongCollector reportStatus(@Key("success") boolean success);

	DurationCollector reportTime(@Key("host") String host);

	DurationCollector reportValue(@Key("type") TaskType type);
}
