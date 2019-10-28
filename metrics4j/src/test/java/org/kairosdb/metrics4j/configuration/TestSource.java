package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.collectors.DoubleCounter;
import org.kairosdb.metrics4j.collectors.LongCounter;

public interface TestSource
{
	LongCounter countSomething();
	DoubleCounter partiallyCountSomething();
	LongCounter countSomethingElse();
}
