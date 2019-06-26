package org.kairosdb.metrics4j;

public interface PostConstruct
{
	//todo pass in context that users can register for context changes
	//
	void init(MetricsContext context);
}
