package org.kairosdb.metrics4j.collectors.helpers;

/**
 When a collectors interface extends ThreadReporter it adds the ability for metrics
 reported to that collectors interface to be associated with each other if reported
 from the same thread.

 Imagine a rest call where you want to report various metrics and different points
 along the call path that you want to tag with the url path the request came in on.
 You would create a collectors interface that extends ThreadReporter and then call
 addTag to set the resource url as a tag.  Then each call on the collectors will put that
 tag.

 Items set on the ThreadReporter will only effect calls to the collectors on the same
 thread.
 */
public interface ThreadReporter
{
	/**
	 Set the time for data points to be reported
	 @param time Timestamp to use when reporting metrics
	 */
	void setReportTime(long time);

	/**
	 This lets you put a tag to all data points submitted to sub interfaces of
	 ThreadReporter
	 @param name Name of tag to add to thread
	 @param value Value of tag to add to thread
	 */
	void addTag(String name, String value);
	void removeTag(String name);
	void clearTags();
	void clearAll();
}
