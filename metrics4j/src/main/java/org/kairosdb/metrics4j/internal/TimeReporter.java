package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.reporting.MetricValue;

import java.time.Duration;

public interface TimeReporter
{
	MetricValue getValue(Duration duration);
}
