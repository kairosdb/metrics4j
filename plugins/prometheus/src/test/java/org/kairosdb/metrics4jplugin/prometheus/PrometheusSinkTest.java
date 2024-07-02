package org.kairosdb.metrics4jplugin.prometheus;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.configuration.MetricConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PrometheusSinkTest
{
	@BeforeEach
	void setUp()
	{

	}

	@AfterEach
	public void cleanup()
	{
		MetricSourceManager.clearConfig();
	}

	@Test
	public void testPrometheusServer() throws IOException, InterruptedException, URISyntaxException
	{
		MetricConfig metricConfig = MetricConfig.parseConfig("prometheus_test.conf", "not.properties");

		MetricSourceManager.setMetricConfig(metricConfig);

		MetricSourceManager.addSource("PrometheusSinkTest", "testPrometheusServer",
				Collections.emptyMap(), "help", () -> 42);

		HttpClient httpClient = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(new URI("http://localhost:9666/metrics")).GET().build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

		System.out.println(response.body());
		assertThat(response.body()).contains("PrometheusSinkTest_testPrometheusServer_value 42.0");

	}
}