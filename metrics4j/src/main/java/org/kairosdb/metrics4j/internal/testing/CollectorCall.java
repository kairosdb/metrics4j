package org.kairosdb.metrics4j.internal.testing;

import java.util.Arrays;
import java.util.Objects;

public class CollectorCall
{
	private final String method;
	private final Object[] values;

	public CollectorCall(String method, Object... values)
	{
		this.method = method;
		this.values = values;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof CollectorCall)) return false;
		CollectorCall that = (CollectorCall) o;
		return Objects.equals(method, that.method) && Objects.deepEquals(values, that.values);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(method, Arrays.hashCode(values));
	}
}
