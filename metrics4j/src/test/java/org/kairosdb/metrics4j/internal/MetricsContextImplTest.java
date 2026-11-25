package org.kairosdb.metrics4j.internal;

import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.sinks.STDOutSink;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsContextImplTest
{

	private final MetricsContextImpl context = new MetricsContextImpl();

	@Test
	public void testGetFormatterByKeyIsEmptyWhenNoFormattersSet()
	{
		List<Formatter> formatter = context.getFormattersForKey(buildKey());

		assertThat(formatter).isEmpty();
	}

	@Test
	public void testGetFormatterByKeyWhenSet()
	{
		Formatter formatter = new DefaultFormatter();
		context.registerFormatter("formatter", formatter);
		context.addFormatterToPath("formatter", buildPath());

		List<Formatter> formatters = context.getFormattersForKey(buildKey());

		assertThat(formatters).containsExactly(formatter);
	}

	@Test
	public void testGetSinkByKeyIsEmptyWhenNoFormattersSet()
	{
		List<MetricSink> sinks = context.getSinksForKey(buildKey());

		assertThat(sinks).isEmpty();
	}

	@Test
	public void testGetSinkByKeyWhenSet()
	{
		MetricSink sink = new STDOutSink();
		context.registerSink("sink", sink);
		context.addSinkToPath("sink", buildPath());

		List<MetricSink> sinks = context.getSinksForKey(buildKey());

		assertThat(sinks).containsExactly(sink);
	}


	private List<String> buildPath()
	{
		return Arrays.asList(this.getClass().getName().split("\\."));
	}

	private ArgKey buildKey()
	{
		// java trick to get the current method in this case "buildKey"
		Method self = new Object() {
		}.getClass().getEnclosingMethod();
		return new MethodArgKey(self, new Object[]{});
	}

}
