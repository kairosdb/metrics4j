package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;

import java.lang.reflect.InvocationHandler;

public interface SourceInvocationHandler extends InvocationHandler
{
	void setCollector(MethodArgKey key, MetricCollector statsObject);
}
