package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.collectors.DoubleCollector;
import org.kairosdb.metrics4j.collectors.LongCollector;
import org.kairosdb.metrics4j.collectors.impl.DoubleCounter;
import org.kairosdb.metrics4j.collectors.impl.LongCounter;

public interface TestSource
{
	LongCollector countSomething();
	DoubleCollector partiallyCountSomething();
	LongCollector countSomethingElse();
	LongCollector chainCount();
}
