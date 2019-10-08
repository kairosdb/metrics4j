package org.kairosdb.metrics4j.configuration;

import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.formatters.Formatter;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.internal.CollectorContainer;
import org.kairosdb.metrics4j.internal.MethodArgKey;
import org.kairosdb.metrics4j.internal.NeverTrigger;
import org.kairosdb.metrics4j.internal.SinkQueue;
import org.kairosdb.metrics4j.internal.TriggerMetricCollection;
import org.kairosdb.metrics4j.sinks.MetricSink;
import org.kairosdb.metrics4j.collectors.Collector;
import org.kairosdb.metrics4j.triggers.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricConfig
{
	private static Logger log = LoggerFactory.getLogger(MetricConfig.class);

	private static final Pattern formatPattern = Pattern.compile("\\$\\{([^\\}]*)\\}");

	private Properties m_properties = new Properties();
	private final Map<String, SinkQueue> m_sinks;
	private final Map<String, Collector> m_collectors;
	private final Map<String, Formatter> m_formatters;
	private final Map<String, TriggerMetricCollection> m_triggers;

	private final Map<List<String>, List<SinkQueue>> m_mappedSinks;
	private final Map<List<String>, List<Collector>> m_mappedCollectors;
	private final Map<List<String>, Formatter> m_mappedFormatters;
	private final Map<List<String>, TriggerMetricCollection> m_mappedTriggers;

	private final Map<List<String>, Map<String, String>> m_mappedTags;

	private final MetricsContext m_context;
	private final List<Closeable> m_closeables;

	private boolean m_shutdownOverride = false;


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

	private String formatValue(String value)
	{
		Matcher matcher = formatPattern.matcher(value);
		StringBuilder sb = new StringBuilder();

		int endLastMatch = 0;
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();

			if (start != endLastMatch)
			{
				sb.append(value.substring(endLastMatch, start));
			}

			String token = matcher.group(1);

			//todo look for values from properties file and from env
			sb.append(m_properties.getProperty(token, "NOTHERE"));

			endLastMatch = end;
		}

		sb.append(value.substring(endLastMatch));

		return sb.toString();
	}

	private void processElement(Element classConfig)
	{
		NamedNodeMap attributes = classConfig.getAttributes();

		for (int i = 0; i < attributes.getLength(); i++)
		{
			Attr attribute = (Attr)attributes.item(i);

			attribute.setValue(formatValue(attribute.getValue()));
		}


		NodeList childNodes = classConfig.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++)
		{
			Node child = childNodes.item(i);
			if (child instanceof Element)
				processElement((Element)child);

			if (child instanceof Text)
			{
				Text txtChild = (Text) child;
				txtChild.replaceWholeText(formatValue(txtChild.getWholeText()));
			}
		}
	}

	private <T> T loadClass(Element classConfig) throws JAXBException
	{
		T ret = null;
		String className = classConfig.getAttribute("class");

		try
		{
			ClassLoader pluginLoader = MetricConfig.class.getClassLoader();

			processElement(classConfig);

			String pluginFolder = classConfig.getAttribute("folder");

			if (!pluginFolder.isEmpty())
			{
				pluginLoader = new PluginClassLoader(getJarsInPath(pluginFolder), pluginLoader);
			}

			Class<?> pluginClass = pluginLoader.loadClass(className);
			JAXBContext context = JAXBContext.newInstance(pluginClass);

			Unmarshaller unmarshaller = context.createUnmarshaller();

			ret = (T) unmarshaller.unmarshal(classConfig);
		}
		catch (ClassNotFoundException | MalformedURLException e)
		{
			throw new ConfigurationException("Unable to locate class '"+className+"' for configuration element '"+classConfig.getTagName()+"'");
		}

		return ret;
	}

	private static URL[] getJarsInPath(String path) throws MalformedURLException
	{
		List<URL> jars = new ArrayList<URL>();
		File libDir = new File(path);
		File[] fileList = libDir.listFiles();
		if(fileList != null)
		{
			for (File f : fileList)
			{
				if (f.getName().endsWith(".jar"))
				{
					jars.add(f.toURI().toURL());
				}
			}
		}

		//System.out.println(jars);
		return jars.toArray(new URL[0]);
	}

	private <T> void registerStuff(Element parent, String childName, BiConsumer<String, T> register) throws JAXBException
	{
		NodeList childList = parent.getElementsByTagName(childName);

		for (int i = 0; i < childList.getLength(); i++)
		{
			Element classElement = (Element)childList.item(i);
			T classInstance = loadClass(classElement);

			register.accept(classElement.getAttribute("name"), classInstance);

			if (classInstance instanceof Closeable)
			{
				m_closeables.add((Closeable)classInstance);
			}
		}
	}

	/*package*/ static List<String> appendSourceName(List<String> parent, String child)
	{
		List<String> copy = new ArrayList<>(parent);

		String[] splitNames = child.split("\\.");

		copy.addAll(Arrays.asList(splitNames));
		return copy;
	}

	/**
	 Recursively parse through the sources elements
	 @param root
	 @param path
	 */
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

				if (node instanceof Element)
				{
					if ("source".equals(nodeName))
					{
						Element element = (Element) node;
						String name = element.getAttribute("name");

						parseSources(element, appendSourceName(path, name));
					}
					else if ("sink".equals(nodeName)) //todo add some attribute to a sink that prevents inheriting sinks
					{
						//need to map to a list of sinks as there can be more than one
						Element sinkElm = (Element) node;
						String ref = sinkElm.getAttribute("ref");

						addSinkToPath(ref, path);
					}
					else if ("collector".equals(nodeName))
					{
						Element collectorElm = (Element) node;
						String ref = collectorElm.getAttribute("ref");

						addCollectorToPath(ref, path);
					}
					else if ("formatter".equals(nodeName))
					{
						Element collectorElm = (Element) node;
						String ref = collectorElm.getAttribute("ref");

						addFormatterToPath(ref, path);
					}
					else if ("trigger".equals(nodeName))
					{
						Element triggerElm = (Element) node;
						String ref = triggerElm.getAttribute("ref");

						addTriggerToPath(ref, path);
					}
					else if ("tags".equals(nodeName))
					{
						Element tagsElm = (Element) node;
						NodeList tags = tagsElm.getChildNodes();

						Map<String, String> pathTags = m_mappedTags.computeIfAbsent(path, (k) -> new HashMap<>());

						for (int j = 0; j < tags.getLength(); j++)
						{
							Node tag = tags.item(j);

							if (tag instanceof Element)
							{
								Element tagElm = (Element)tag;

								String key = tagElm.getAttribute("key");
								String value = tagElm.getAttribute("value");

								pathTags.put(key, value);
							}
						}
					}
					else
					{
						throw new ConfigurationException("Unknown configuration element: " + nodeName);
					}
				}


			}
		}
	}

	/**
	 Parse through the root level elements of the metrics4j xml file
	 @param configInputStream
	 @return
	 @throws ParserConfigurationException
	 @throws IOException
	 @throws SAXException
	 */
	public static MetricConfig parseConfig(InputStream propertiesInputStream, InputStream configInputStream) throws ParserConfigurationException, IOException, SAXException
	{
		//todo break up this method so it can be built in parts by unit tests
		MetricConfig ret = new MetricConfig();

		if (propertiesInputStream != null)
		{
			Properties props = new Properties();
			props.load(propertiesInputStream);

			ret.setProperties(props);
		}

		//todo add system properties and add env

		if (configInputStream != null)
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document configDoc = db.parse(configInputStream);

			Element root = configDoc.getDocumentElement();

			try
			{
				//Parse out the sinks
				Element sinks = getFirstElement(root, "sinks");
				ret.registerStuff(sinks, "sink", ret::registerSink);

				Element collectors = getFirstElement(root, "collectors");
				ret.registerStuff(collectors, "collector", ret::registerCollector);

				Element formatters = getFirstElement(root, "formatters");
				ret.registerStuff(formatters, "formatter", ret::registerFormatter);

				Element triggers = getFirstElement(root, "triggers");
				ret.registerStuff(triggers, "trigger", ret::registerTrigger);

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


	/*package*/
	public MetricConfig()
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
		m_closeables = new ArrayList<>();
		m_mappedTags = new HashMap<>();


		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				if (!m_shutdownOverride)
					shutdown();
			}
		}));
	}

	private void shutdown()
	{
		for (Closeable closeable : m_closeables)
		{
			try
			{
				closeable.close();
			}
			catch (Exception e)
			{
				log.error("Error closing "+closeable.getClass().getName(), e);
			}
		}
	}

	public ShutdownHookOverride getShutdownHookOverride()
	{
		m_shutdownOverride = true;
		return MetricConfig.this::shutdown;
	}

	public void addCollectorToPath(String name, List<String> path)
	{
		Collector collector = m_collectors.get(name);
		if (collector == null)
			throw new MissingReferenceException("collector", name);

		List<Collector> collectors = m_mappedCollectors.computeIfAbsent(path, (k) -> new ArrayList<>());
		collectors.add(collector);
	}

	public void addSinkToPath(String name, List<String> path)
	{
		SinkQueue sinkQueue = m_sinks.get(name);
		if (sinkQueue == null)
			throw new MissingReferenceException("sink", name);

		List<SinkQueue> sinkQueues = m_mappedSinks.computeIfAbsent(path, (k) -> new ArrayList<>());
		sinkQueues.add(sinkQueue);
	}

	public void addFormatterToPath(String name, List<String> path)
	{
		Formatter formatter = m_formatters.get(name);
		if (formatter == null)
			throw new MissingReferenceException("formatter", name);

		m_mappedFormatters.put(path, formatter);
	}

	public void addTriggerToPath(String name, List<String> path)
	{
		TriggerMetricCollection trigger = m_triggers.get(name);
		if (trigger == null)
			throw new MissingReferenceException("trigger", name);

		m_mappedTriggers.put(path, trigger);
	}

	public void registerSink(String name, MetricSink sink)
	{
		sink.init(m_context);
		m_sinks.put(name, new SinkQueue(sink));
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
		m_triggers.put(name, new TriggerMetricCollection(trigger));
	}

	public MetricSink getSink(String name)
	{
		return m_sinks.get(name).getSink();
	}


	private <R> R findObject(ArgKey key, Function<List<String>, R> getter)
	{
		R ret = null;
		List<String> configPath = key.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));
			ret = getter.apply(searchPath);
			if (ret != null)
				break;
		}

		return ret;
	}

	private <R> List<R> findAllObjects(ArgKey key, Function<List<String>, List<R>> getter)
	{
		List<R> ret = new ArrayList<>();
		List<String> configPath = key.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));
			List<R> objects = getter.apply(searchPath);
			if (objects != null)
				ret.addAll(objects);
		}

		return ret;
	}

	public Iterator<Collector> getCollectorsForKey(ArgKey key)
	{
		List<Collector> ret = findAllObjects(key, m_mappedCollectors::get);

		return ret.iterator();
	}

	public Formatter getFormatterForKey(ArgKey key)
	{
		return findObject(key, m_mappedFormatters::get);
	}

	public List<SinkQueue> getSinkQueues(ArgKey key)
	{
		List<SinkQueue> ret = findAllObjects(key, m_mappedSinks::get);

		return ret;
	}

	public TriggerMetricCollection getTriggerForKey(ArgKey key)
	{
		TriggerMetricCollection triggerMetricCollection = findObject(key, m_mappedTriggers::get);
		if (triggerMetricCollection == null)
		{
			triggerMetricCollection = new TriggerMetricCollection(new NeverTrigger());
		}

		return triggerMetricCollection;
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
		return m_triggers.get(name).getTrigger();
	}

	public MetricsContext getContext()
	{
		return m_context;
	}

	public void setProperties(Properties properties)
	{
		m_properties = properties;
	}

	public void assignCollector(ArgKey key, CollectorContainer collectorContainer)
	{
		Formatter formatter = getFormatterForKey(key);
		if (formatter != null)
			collectorContainer.setFormatter(formatter);

		List<SinkQueue> sinkQueues = getSinkQueues(key);
		collectorContainer.addSinkQueue(sinkQueues);

		TriggerMetricCollection trigger = getTriggerForKey(key);
		trigger.addCollector(collectorContainer);
	}

	public Map<String, String> getTagsForKey(MethodArgKey argKey)
	{
		Map<String, String> ret = new HashMap<>();
		List<String> configPath = argKey.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));
			Map<String, String> pathTags = m_mappedTags.getOrDefault(searchPath, new HashMap<>());

			for (String key : pathTags.keySet())
			{
				ret.putIfAbsent(formatValue(key), formatValue(pathTags.get(key)));
			}
		}

		return ret;
	}
}
