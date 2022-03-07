package org.kairosdb.metrics4j.plugins;

public class TestImpl implements TestMXBean
{
	@Override
	public int getIntCount()
	{
		System.out.println("CALLED "+this);
		return 1;
	}

	@Override
	public long getLongCount()
	{
		return 123;
	}

	@Override
	public float getFloatValue()
	{
		return 1.1f;
	}

	@Override
	public double getDoubleValue()
	{
		return 3.14159;
	}
}
