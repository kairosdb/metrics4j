package org.kairosdb.metrics4j.internal.testing;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.collectors.helpers.AllCollectors;
import org.kairosdb.metrics4j.collectors.helpers.TimerCollector;
import org.kairosdb.metrics4j.internal.MethodArgKey;
import org.kairosdb.metrics4j.reporting.MetricReporter;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.kairosdb.metrics4j.internal.testing.CollectorRecorder.PUT;

public class VerifyCollector extends TimerCollector implements AllCollectors
{
	private final SourceInvocationHandlerRecorder m_recorder;
	private final MethodArgKey m_invokedMethod;
	private final int m_times;

	public VerifyCollector(SourceInvocationHandlerRecorder recorder, MethodArgKey invokedMethod, int times)
	{
		m_recorder = recorder;
		m_invokedMethod = invokedMethod;
		m_times = times;
	}

	private void verify(CollectorCall myCall)
	{
		CollectorRecorder collectorRecorder = m_recorder.getCollectorRecorder(m_invokedMethod);
		List<CollectorCall> callList = collectorRecorder.getCallList();

		List<CollectorCall> matches = callList.stream()
				.filter(cc -> (myCall.equals(cc))).collect(Collectors.toList());

		if (matches.size() != m_times)
			throw new VerifyException("Wanted "+ m_times + " calls but received "+ matches.size());
	}

	@Override
	public void put(double value)
	{
		CollectorCall myCall = new CollectorCall(PUT, value);
		verify(myCall);
	}

	@Override
	public void put(Instant time, double value)
	{
		CollectorCall myCall = new CollectorCall(PUT, time, value);
		verify(myCall);
	}

	@Override
	public void put(Duration duration)
	{
		CollectorCall myCall = new CollectorCall(PUT, duration);
		verify(myCall);
	}

	@Override
	public void put(Instant time, Duration duration)
	{
		CollectorCall myCall = new CollectorCall(PUT, time, duration);
		verify(myCall);
	}

	@Override
	public void put(long value)
	{
		CollectorCall myCall = new CollectorCall(PUT, value);
		verify(myCall);
	}

	@Override
	public void put(Instant time, long value)
	{
		CollectorCall myCall = new CollectorCall(PUT, time, value);
		verify(myCall);
	}

	@Override
	public void put(String value)
	{
		CollectorCall myCall = new CollectorCall(PUT, value);
		verify(myCall);
	}

	@Override
	public void put(Instant time, String value)
	{
		CollectorCall myCall = new CollectorCall(PUT, time, value);
		verify(myCall);
	}

	@Override
	public void put(Instant value)
	{
		CollectorCall myCall = new CollectorCall(PUT, value);
		verify(myCall);
	}

	@Override
	public void put(Instant time, Instant value)
	{
		CollectorCall myCall = new CollectorCall(PUT, time, value);
		verify(myCall);
	}

	@Override
	public Collector clone()
	{
		return null;
	}

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetric(MetricReporter metricReporter)
	{

	}

	@Override
	public void setContextProperties(Map<String, String> contextProperties)
	{

	}
}
