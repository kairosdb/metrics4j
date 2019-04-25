package org.kairosdb.metrics4j.configuration;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class MetricConfigTest
{
	@Test
	public void testReadingConfiguration() throws IOException, SAXException, ParserConfigurationException
	{
		InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("test_config.xml");

		MetricConfig.parseConfig(is);
	}
}