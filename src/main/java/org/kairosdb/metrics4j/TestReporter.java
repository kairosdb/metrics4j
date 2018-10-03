package org.kairosdb.metrics4j;

public interface TestReporter
{
	SparseStats reportSize(@key("") String value)
}
