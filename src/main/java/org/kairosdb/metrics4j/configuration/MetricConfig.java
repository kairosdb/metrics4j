package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.triggers.Trigger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;

public class MetricConfig
{
	private static Element getFirstElement(Element parent, String tag)
	{
		Node ret = null;

		NodeList list = parent.getElementsByTagName(tag);
		if (list != null && list.getLength() != 0)
		{
			ret = list.item(0);
		}

		return (Element)ret;
	}

	private static <T> T loadClass(Element classConfig) throws JAXBException, ClassNotFoundException
	{
		T ret = null;
		String className = classConfig.getAttribute("class");

		Class<?> pluginClass = MetricConfig.class.getClassLoader().loadClass(className);
		JAXBContext context = JAXBContext.newInstance(pluginClass);


		return ret;
	}

	private static <T> void registerStuff(Element parent, String childName, BiConsumer<String, T> register)
	{
		NodeList childList = parent.getElementsByTagName(childName);

		for (int i = 0; i < childList.getLength(); i++)
		{
			Element classElement = (Element)childList.item(i);
			//T classInstance = loadClass(classElement);

			//register.accept(classElement.getAttribute("name"), classInstance);
		}
	}

	public static MetricConfig parseConfig(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException
	{
		MetricConfig ret = new MetricConfig();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document configDoc = db.parse(inputStream);

		Element root = configDoc.getDocumentElement();

		//Parse out the sinks
		Element sinks = getFirstElement(root, "sinks");
		registerStuff(sinks, "sink", ret::registerSink);

		Element collectors = getFirstElement(root, "collectors");
		registerStuff(collectors, "collector", ret::registerCollector);

		Element formatters = getFirstElement(root, "formatters");
		registerStuff(formatters, "formatter", ret::registerFormatter);

		Element triggers = getFirstElement(root, "triggers");
		registerStuff(triggers, "trigger", ret::registerTrigger);

		return null;
	}


	public MetricConfig()
	{
	}

	public void registerSink(String name, MetricSink sink)
	{
		System.out.println("Got a sink "+name);
	}

	public void registerCollector(String name, Collector collector)
	{

	}

	public void registerFormatter(String name, Formatter formatter)
	{
	}

	public void registerTrigger(String name, Trigger trigger)
	{

	}
}
