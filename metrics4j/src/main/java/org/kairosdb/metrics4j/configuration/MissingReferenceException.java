package org.kairosdb.metrics4j.configuration;

public class MissingReferenceException extends ConfigurationException
{
	public MissingReferenceException(String element, String ref)
	{
		super("Missing reference '"+ref+"' for element '"+element+"'");
	}
}
