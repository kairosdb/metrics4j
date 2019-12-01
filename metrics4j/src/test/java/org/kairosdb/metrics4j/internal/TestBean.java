package org.kairosdb.metrics4j.internal;

import lombok.Setter;

import java.time.Duration;
import java.util.List;
import java.util.Set;

public class TestBean
{
	@Setter
	int intValue;

	@Setter
	Integer intObjValue;

	@Setter
	double doubleValue;

	@Setter
	Double doubleObjValue;

	@Setter
	boolean booleanValue;

	@Setter
	long longValue;

	@Setter
	String stringValue;

	@Setter
	Duration duration;

	@Setter
	List<String> listOfStr;

	@Setter
	Set<Integer> setOfInt;
}
