package org.kairosdb.metrics4j.internal;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ArgKeyTest
{

	@Test
	public void getConfigPath() throws NoSuchMethodException
	{
		Method method = this.getClass().getMethod("getConfigPath");
		ArgKey argKey = new MethodArgKey(method, new Object[0]);

		assertThat(argKey.getConfigPath()).isEqualTo(
				Arrays.asList("org", "kairosdb", "metrics4j", "internal", "ArgKeyTest", "getConfigPath")
		);
	}
}