package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.impl.DoubleCounter;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;

public interface TestSource
{
	LongCollector countSomething();

	LongCollector countSomethingWithTag(@Key("host") String host);
	DoubleCollector partiallyCountSomething();
	LongCollector countSomethingElse();
	LongCollector chainCount();
	LongCollector countOverride(@Key("client")String client);
	LongCollector countNoOverride(@Key("client")String client);
	LongCollector testContext(@Key("tag")String tag);
}
