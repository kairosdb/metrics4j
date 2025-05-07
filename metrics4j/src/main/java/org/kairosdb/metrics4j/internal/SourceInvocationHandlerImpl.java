package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.collectors.MetricCollector;

import java.lang.reflect.InvocationHandler;

public interface SourceInvocationHandlerImpl extends InvocationHandler
{
	void setCollector(MethodArgKey key, MetricCollector statsObject);
}
