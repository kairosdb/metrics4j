package org.kairosdb.metrics4j.internal.adapters;

import org.kairosdb.metrics4j.Snapshot;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class MethodSnapshotAdapter implements Snapshot
{
	private static final Logger log = LoggerFactory.getLogger(MethodSnapshotAdapter.class);

	private final Object m_object;
	private final Method m_method;

	public MethodSnapshotAdapter(Object object, Method method)
	{
		m_object = object;
		m_method = method;
	}

	@Override
	public void run()
	{
		try
		{
			m_method.invoke(m_object);
		}
		catch (Exception e)
		{
			log.error("Unable to call snapshot method on "+m_object.getClass().getName(), e);
		}
	}
}
