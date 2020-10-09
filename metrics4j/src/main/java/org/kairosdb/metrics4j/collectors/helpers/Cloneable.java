package org.kairosdb.metrics4j.collectors.helpers;

public class Cloneable implements java.lang.Cloneable
{
	@Override
	protected Object clone()
	{
		Object ret = null;
		try
		{
			ret = super.clone();
		}
		catch (CloneNotSupportedException e)
		{
		}

		return ret;
	}
}
