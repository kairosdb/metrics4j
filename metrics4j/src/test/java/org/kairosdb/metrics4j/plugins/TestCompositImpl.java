package org.kairosdb.metrics4j.plugins;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.lang.management.MemoryUsage;

public class TestCompositImpl implements TestCompositMXBean
{
	@Override
	public CompositeValues getCompositeValues()
	{
		return new CompositeValues();
		/*try
		{
			String[] itemNames = new String[]{"myLong", "myInt", "myFloat", "myDouble"};
			CompositeType myType = new CompositeType("MyType", "description", itemNames,
					new String[]{"description", "description", "description", "description"},
					new OpenType[]{SimpleType.LONG, SimpleType.INTEGER, SimpleType.FLOAT, SimpleType.DOUBLE});

			return new CompositeDataSupport(myType, new String[]{"myLong", "myInt", "myFloat", "myDouble"},
					new Object[]{1234L, 42, 1.1f, 3.14159});
		}
		catch (OpenDataException e)
		{
			e.printStackTrace();
		}

		return null;*/
	}
}
