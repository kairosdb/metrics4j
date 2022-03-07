package org.kairosdb.metrics4j.plugins;

import javax.management.DynamicMBean;

public interface TestMXBean
{
	int getIntCount();
	long getLongCount();
	float getFloatValue();
	double getDoubleValue();
}
