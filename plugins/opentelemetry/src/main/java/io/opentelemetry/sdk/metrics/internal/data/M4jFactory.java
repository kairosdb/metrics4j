package io.opentelemetry.sdk.metrics.internal.data;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.resources.Resource;

public interface M4jFactory
{
	static ImmutableMetricData metricDataCreate(
			Resource resource,
			InstrumentationScopeInfo instrumentationScopeInfo,
			String name,
			String description,
			String unit,
			MetricDataType type,
			Data<?> data)
	{
		return ImmutableMetricData.create(
				resource,
				instrumentationScopeInfo,
				name,
				description,
				unit,
				type,
				data);
	}
}
