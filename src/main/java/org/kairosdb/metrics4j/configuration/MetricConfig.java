package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.triggers.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MetricConfig
{
	private static Logger log = LoggerFactory.getLogger(MetricConfig.class);


	private final Map<String, MetricSink> m_sinks;
	private final Map<String, Collector> m_collectors;
	private final Map<String, Formatter> m_formatters;
	private final Map<String, Trigger> m_triggers;
	private final Map<List<String>, MetricSink> m_mappedSinks;
	private final Map<List<String>, Collector> m_mappedCollectors;
	private final Map<List<String>, Formatter> m_mappedFormatters;
	private final Map<List<String>, Trigger> m_mappedTriggers;
	private final MetricsContext m_context;

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

	private static <T> T loadClass(Element classConfig) throws JAXBException
	{
		T ret = null;
		String className = classConfig.getAttribute("class");

		try
		{
			Class<?> pluginClass = MetricConfig.class.getClassLoader().loadClass(className);
			JAXBContext context = JAXBContext.newInstance(pluginClass);

			Unmarshaller unmarshaller = context.createUnmarshaller();

			ret = (T) unmarshaller.unmarshal(classConfig);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ConfigurationException("Unable to locate class '"+className+"' for configuration element '"+classConfig.getTagName()+"'");
		}

		return ret;
	}

	private static <T> void registerStuff(Element parent, String childName, BiConsumer<String, T> register) throws JAXBException
	{
		NodeList childList = parent.getElementsByTagName(childName);

		for (int i = 0; i < childList.getLength(); i++)
		{
			Element classElement = (Element)childList.item(i);
			T classInstance = loadClass(classElement);

			register.accept(classElement.getAttribute("name"), classInstance);
		}
	}

	private static List<String> appendSourceName(List<String> parent, String child)
	{
		List<String> copy = new ArrayList<>(parent);

		copy.add(child);
		return copy;
	}

	private void parseSources(Element root, List<String> path)
	{
		if (root == null)
			throw new ConfigurationException("No 'sources' element in your configuration");

		NodeList childNodes = root.getChildNodes();

		if (childNodes != null)
		{
			for (int i = 0; i < childNodes.getLength(); i++)
			{
				Node node = childNodes.item(i);
				String nodeName = node.getNodeName();

				if ("source".equals(nodeName))
				{
					Element element = (Element) node;
					String name = element.getAttribute("name");

					parseSources(element, appendSourceName(path, name));
				}

				if ("sink".equals(nodeName))
				{
				}

				if ("collector".equals(nodeName))
				{
					Element collectorElm = (Element)node;
					String ref = collectorElm.getAttribute("ref");

					Collector collector = m_collectors.get(ref);
					if (collector == null)
						throw new MissingReferenceException("collector", ref);

					m_mappedCollectors.put(path, collector);
				}

				if ("formatter".equals(nodeName))
				{
				}

				if ("trigger".equals(nodeName))
				{
					Element triggerElm = (Element)node;
					String ref = triggerElm.getAttribute("ref");

					Trigger trigger = m_triggers.get(ref);
					if (trigger == null)
						throw new MissingReferenceException("collector", ref);

					m_mappedTriggers.put(path, trigger);
				}

			}
		}
	}

	public static MetricConfig parseConfig(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException
	{
		MetricConfig ret = new MetricConfig();

		if (inputStream != null)
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document configDoc = db.parse(inputStream);

			Element root = configDoc.getDocumentElement();

			try
			{
				//Parse out the sinks
				Element sinks = getFirstElement(root, "sinks");
				registerStuff(sinks, "sink", ret::registerSink);

				Element collectors = getFirstElement(root, "collectors");
				registerStuff(collectors, "collector", ret::registerCollector);

				Element formatters = getFirstElement(root, "formatters");
				registerStuff(formatters, "formatter", ret::registerFormatter);

				Element triggers = getFirstElement(root, "triggers");
				registerStuff(triggers, "trigger", ret::registerTrigger);

				//todo parse through sources and add them to a map
				ret.parseSources(getFirstElement(root, "sources"), new ArrayList<>());
			}
			catch (JAXBException e)
			{
				log.error("Error parsing config file", e);
				throw new RuntimeException(e);
			}
		}

		return ret;
	}


	private MetricConfig()
	{
		m_sinks = new HashMap<>();
		m_collectors = new HashMap<>();
		m_formatters = new HashMap<>();
		m_triggers = new HashMap<>();
		m_context = new MetricsContext();
		m_mappedCollectors = new HashMap<>();
		m_mappedFormatters = new HashMap<>();
		m_mappedSinks = new HashMap<>();
		m_mappedTriggers = new HashMap<>();
	}

	public void registerSink(String name, MetricSink sink)
	{
		sink.init(m_context);
		m_sinks.put(name, sink);
	}

	public void registerCollector(String name, Collector collector)
	{
		collector.init(m_context);
		m_collectors.put(name, collector);
	}

	public void registerFormatter(String name, Formatter formatter)
	{
		formatter.init(m_context);
		m_formatters.put(name, formatter);
	}

	public void registerTrigger(String name, Trigger trigger)
	{
		trigger.init(m_context);
		m_triggers.put(name, trigger);
	}

	public MetricSink getSink(String name)
	{
		return m_sinks.get(name);
	}

	public Collector getCollectorForKey(ArgKey key)
	{
		Collector ret = null;
		List<String> configPath = key.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));
			ret = m_mappedCollectors.get(searchPath);
			if (ret != null)
				break;
		}

		return ret;
	}

	public Collector getCollector(String name)
	{
		Collector collector = m_collectors.get(name);
		//todo clone collector
		return collector;
	}

	public Formatter getFormatter(String name)
	{
		return m_formatters.get(name);
	}

	public Trigger getTrigger(String name)
	{
		return m_triggers.get(name);
	}

	public MetricsContext getContext()
	{
		return m_context;
	}
}
