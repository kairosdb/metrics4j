package org.kairosdb.metrics4j.internal;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.jupiter.api.Test;

import java.beans.IntrospectionException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class BeanInjectorTest
{
	@Test
	public void testConfigTypes() throws IntrospectionException
	{
		Config config = ConfigFactory.parseResources("test_bean.conf");

		BeanInjector injector = new BeanInjector("test", TestBean.class);

		TestBean instance = (TestBean)injector.createInstance(config);

		assertThat(instance.intValue).isEqualTo(42);
		assertThat(instance.intObjValue).isEqualTo(123);
		assertThat(instance.doubleValue).isEqualTo(1.234);
		assertThat(instance.doubleObjValue).isEqualTo(3.14159);
		assertThat(instance.booleanValue).isTrue();
		assertThat(instance.longValue).isEqualTo(123456789123456789L);
		assertThat(instance.stringValue).isEqualTo("Happy to be here");
		assertThat(instance.duration).isEqualTo(Duration.ofHours(42));
		assertThat(instance.listOfStr).containsExactly("one", "two", "three");
		assertThat(instance.setOfInt).containsExactly(1, 2, 3, 4);
	}
}