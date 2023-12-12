package org.kairosdb.metrics4j.sinks;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.DefaultFormatter;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.FormattedMetric;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class STDOutSink implements MetricSink
{
	private static final Formatter DEFAULT_FORMATTER = new DefaultFormatter();

	@Override
	public void init(MetricsContext context)
	{

	}

	@Override
	public void reportMetrics(List<FormattedMetric> metrics)
	{
		for (FormattedMetric metric : metrics)
		{
			Map<String, String> tags = metric.getTags();
			StringBuilder tagBuilder = new StringBuilder();
			tagBuilder.append("{");
			boolean first = true;
			for (String tag : tags.keySet())
			{
				if (!first)
					tagBuilder.append(',');
				tagBuilder.append(tag).append("=").append(tags.get(tag));
				first = false;
			}
			tagBuilder.append("}");

			String tagString = tagBuilder.toString();

			for (FormattedMetric.Sample sample : metric.getSamples())
			{
				System.out.println(MessageFormat.format("metric={0}, time={1}, value={2}, tags={3}", sample.getMetricName(),
						sample.getTime(), sample.getValue().getValueAsString(), tagString));
			}

		}
	}

	@Override
	public Formatter getDefaultFormatter()
	{
		return DEFAULT_FORMATTER;
	}
}
