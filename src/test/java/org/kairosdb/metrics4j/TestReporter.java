package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.stats.Counter;

public interface TestReporter
{
	Counter reportSize(@Key("host") String host);
}
