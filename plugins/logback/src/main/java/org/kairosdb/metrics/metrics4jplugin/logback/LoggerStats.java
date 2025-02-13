package org.kairosdb.metrics.metrics4jplugin.logback;

import org.kairosdb.metrics4j.annotation.Help;
import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.collectors.LongCollector;

public interface LoggerStats
{
	@Help("Number of calls made to the logger, with the level as a tag")
	LongCollector logCount(@Key("name") String name, @Key("level") String level, @Key("logger")String logger);

	@Help("Number of calls made to the trace logger")
	LongCollector trace(@Key("name") String name, @Key("logger")String logger);
	@Help("Number of calls made to the debug logger")
	LongCollector debug(@Key("name") String name, @Key("logger")String logger);
	@Help("Number of calls made to the info logger")
	LongCollector info(@Key("name") String name, @Key("logger")String logger);
	@Help("Number of calls made to the warn logger")
	LongCollector warn(@Key("name") String name, @Key("logger")String logger);
	@Help("Number of calls made to the error logger")
	LongCollector error(@Key("name") String name, @Key("logger")String logger);
}
