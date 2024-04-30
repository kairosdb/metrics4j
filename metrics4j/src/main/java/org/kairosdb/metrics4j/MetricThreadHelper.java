package org.kairosdb.metrics4j;

import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 Used to set tags and report time for metrics reported on the current thread.
 */
public class MetricThreadHelper
{

	private static class CurrentTags extends ThreadLocal<SortedMap<String, String>>
	{
		@Override
		protected synchronized SortedMap<String, String> initialValue()
		{
			return new TreeMap<>();
		}
	}

	private static class ReporterTime extends ThreadLocal<Instant>
	{
		@Override
		protected synchronized Instant initialValue()
		{
			return (Instant.MIN);
		}
	}


	private static final CurrentTags s_currentTags = new CurrentTags();
	private static final ReporterTime s_reportTime = new ReporterTime();

	private MetricThreadHelper()
	{
	}

	/**
	 Sets the time which metrics will be reported with.  This only has effect on
	 non aggregated collectors like the BagCollector.
	 @param time Timestamp to report the metric
	 */
	public static void setReportTime(Instant time)
	{
		s_reportTime.set(time);
	}

	/**
	 Clears the report time from the thread.  It is a good idea to pair this
	 with the setReportTime so you don't accidentally override the metric report
	 time by other metrics this thread may report in the future.
	 */
	public static void clearReportTime()
	{
		s_reportTime.set(Instant.MIN);
	}

	/**
	 Returns the set report time or Instance.MIN if not set.
	 @return Report time for metric
	 */
	public static Instant getReportTime()
	{
		return s_reportTime.get();
	}

	/**
	 Adds a tag to all metrics that will be reported from the current thread.
	 @param name Name of tag
	 @param value Value of tag
	 */
	public static void addTag(String name, String value)
	{
		s_currentTags.get().put(name, value);
	}

	/**
	 Returns a value of a tag if set on the thread, null otherwise.
	 * @param name Name of tag
	 * @return Value of tag
	 */
	public static String getTag(String name)
	{
		return s_currentTags.get().get(name);
	}

	/**
	 Removes a single tag from the thread.
	 @param name Name of tag
	 */
	public static void removeTag(String name)
	{
		s_currentTags.get().remove(name);
	}

	/**
	 Clears all tags from the thread.  It is a good idea to add this to the end
	 of any calls a thread is making to make sure the thread doesn't take the tags
	 with it when doing other work.
	 */
	public static void clearTags()
	{
		s_currentTags.get().clear();
	}

	/**
	 Iterator of all tags set on the thread.
	 @return Iterator of all tag keys and values
	 */
	public static Iterator<Map.Entry<String, String>> getTagIterator()
	{
		return s_currentTags.get().entrySet().iterator();
	}
}
