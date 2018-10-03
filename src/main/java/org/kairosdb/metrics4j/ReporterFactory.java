package org.kairosdb.metrics4j;

import java.lang.reflect.Proxy;

public class ReporterFactory
{
	public static <T> T createReporter(Class<T> tClass)
	{
		//Need to create invocationHandler for tClass using
		//invocation handler needs to setup for each method
		//in tClass an appropriate stats object and registers the stats
		//object in some central registry.

		//On some schedule the stats objects in the registry and scrapped
		//for their data and that data is sent to any configured reporter endpoints (kairos, influx, etc..)
		//This all needs to be based on configuration that is dynamically loaded
		//so that it can change at runtime

		//Extra challenge - ability to have reporter endpoints get data at different
		//resolutions.  ex one endpoint gets data every minute and another endpoint
		//gets data every 10 min.  Challenge is in the stats objects if they reset
		//any state or not

		Proxy.newProxyInstance(tClass.getClassLoader(), new Class[]{tClass},
				invocationHandler);

	}

}
