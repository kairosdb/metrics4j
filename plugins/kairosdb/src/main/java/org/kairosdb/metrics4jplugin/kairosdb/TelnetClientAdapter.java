package org.kairosdb.metrics4jplugin.kairosdb;

import org.kairosdb.client.Client;
import org.kairosdb.client.TelnetClient;
import org.kairosdb.client.builder.MetricBuilder;
import org.kairosdb.client.builder.QueryBuilder;
import org.kairosdb.client.builder.QueryTagBuilder;
import org.kairosdb.client.builder.RollupBuilder;
import org.kairosdb.client.builder.RollupTask;
import org.kairosdb.client.response.JsonResponseHandler;
import org.kairosdb.client.response.QueryResponse;
import org.kairosdb.client.response.QueryTagResponse;

import java.io.IOException;
import java.util.List;

public class TelnetClientAdapter implements Client
{
	private final TelnetClient m_client;

	public TelnetClientAdapter(TelnetClient client)
	{
		m_client = client;
	}

	@Override
	public RollupTask createRollupTask(RollupBuilder rollupBuilder)
	{
		return null;
	}

	@Override
	public void deleteRollupTask(String s)
	{

	}

	@Override
	public List<RollupTask> getRollupTasks()
	{
		return null;
	}

	@Override
	public RollupTask getRollupTask(String s)
	{
		return null;
	}

	@Override
	public Object getMetricNames()
	{
		return null;
	}

	@Override
	public List<String> getStatus()
	{
		return null;
	}

	@Override
	public int getStatusCheck()
	{
		return 0;
	}

	@Override
	public <T> T query(QueryBuilder queryBuilder, JsonResponseHandler<T> jsonResponseHandler)
	{
		return null;
	}

	@Override
	public QueryResponse query(QueryBuilder queryBuilder)
	{
		return null;
	}

	@Override
	public QueryTagResponse queryTags(QueryTagBuilder queryTagBuilder)
	{
		return null;
	}

	@Override
	public <T> T queryTags(QueryTagBuilder queryTagBuilder, JsonResponseHandler<T> jsonResponseHandler)
	{
		return null;
	}

	@Override
	public void pushMetrics(MetricBuilder metricBuilder)
	{
		m_client.putMetrics(metricBuilder);
	}

	@Override
	public void deleteMetric(String s)
	{

	}

	@Override
	public void delete(QueryBuilder queryBuilder)
	{

	}

	@Override
	public String getVersion()
	{
		return null;
	}

	@Override
	public void registerCustomDataType(String s, Class aClass)
	{

	}

	@Override
	public Class getDataPointValueClass(String s)
	{
		return null;
	}

	@Override
	public void close() throws IOException
	{
		m_client.shutdown();
	}
}
