package org.kairosdb.metrics4j.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.internal.BeanInjector;
import org.kairosdb.metrics4j.internal.MetricsContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricConfig
{
	public static final String CLASS_PROPERTY = "_class";
	public static final String FOLDER_PROPERTY = "_folder";
	public static final String DUMP_FILE = "_dump-file";
	public static final String METRIC_NAME = "_metric-name";


	private static Logger log = LoggerFactory.getLogger(MetricConfig.class);

	private static final Pattern formatPattern = Pattern.compile("\\%\\{([^\\}]*)\\}");

	private Properties m_properties = new Properties();

	private final Map<List<String>, Map<String, String>> m_mappedTags;
	private final Map<List<String>, Map<String, String>> m_mappedProps;
	private final Map<List<String>, String> m_mappedMetricNames;

	private final MetricsContextImpl m_context;
	private final List<Closeable> m_closeables;

	private boolean m_shutdownOverride = false;
	private boolean m_dumpMetrics = false;
	private String m_dumpFile;
	private Map<String, Object> m_dumpConfig;



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
				sb.append(value, endLastMatch, start);
			}

			String token = matcher.group(1);

			//todo look for values from properties file and from env
			sb.append(m_properties.getProperty(token, "%{"+token+"}"));

			endLastMatch = end;
		}

		sb.append(value.substring(endLastMatch));

		return sb.toString();
	}


	private <T> T loadClass(Config config, String objName)
	{
		T ret = null;
		String className = config.getString(CLASS_PROPERTY);

		try
		{
			ClassLoader pluginLoader = MetricConfig.class.getClassLoader();

			if (config.hasPath(FOLDER_PROPERTY))
			{
				String pluginFolder = config.getString(FOLDER_PROPERTY);
				pluginLoader = new PluginClassLoader(getJarsInPath(pluginFolder), pluginLoader);
			}

			Class<?> pluginClass = pluginLoader.loadClass(className);

			BeanInjector beanInjector = new BeanInjector(objName, pluginClass);

			ret = (T) beanInjector.createInstance(config);
		}
		catch (ClassNotFoundException | MalformedURLException | IntrospectionException e)
		{
			throw new ConfigurationException("Unable to load plugin '"+objName+"' '"+className+"' for configuration element '"+config.origin().lineNumber()+"'");
		}

		return ret;
	}

	private static URL[] getJarsInPath(String path) throws MalformedURLException
	{
		List<URL> jars = new ArrayList<>();
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

	private <T> void registerStuff(Config configs, BiConsumer<String, T> register)
	{
		//Get entry keys
		Set<String> keys = new HashSet<>();
		for (Map.Entry<String, ConfigValue> config : configs.entrySet())
		{
			keys.add(config.getKey().split("\\.")[0]);
		}

		for (String name : keys)
		{

			T classInstance = loadClass(configs.getConfig(name), name);

			register.accept(name, classInstance);

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

	private String combinePath(String[] path, int limit)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < limit; i++)
		{
			sb.append(path[i]).append(".");
		}

		sb.append(path[limit]);

		return sb.toString();
	}

	private List<String> createList(String[] arr, int limit)
	{
		List<String> ret = new ArrayList<>();
		for (int i = 0; i <= limit; i++)
		{
			ret.add(arr[i]);
		}

		return ret;
	}

	/**
	 Recursively parse through the sources elements
	 @param root
	 */
	private void parseSources(Config root)
	{
		if (root == null)
			throw new ConfigurationException("No 'sources' element in your configuration");

		Set<Map.Entry<String, ConfigValue>> entries = root.entrySet();
		for (Map.Entry<String, ConfigValue> entry : entries)
		{
			String[] path = entry.getKey().split("\\.");

			for (int i = (path.length -1); i >= 0 ; i--)
			{
				if (path[i].startsWith("_"))
				{
					String internalProp = path[i];
					if (internalProp.equals(METRIC_NAME))
					{
						String combinedPath = combinePath(path, i);

						String metricName = root.getString(combinedPath);

						if (!metricName.isEmpty())
							m_mappedMetricNames.put(createList(path, i-1), metricName);
					}
					else if (internalProp.equals("_sink"))
					{
						//need to map to a list of sinks as there can be more than one
						String ref = root.getString(combinePath(path, i));

						m_context.addSinkToPath(ref, createList(path, i-1));
					}
					else if (internalProp.equals("_collector"))
					{
						String ref = root.getString(combinePath(path, i));
						m_context.addCollectorToPath(ref, createList(path, i-1));
					}
					else if (internalProp.equals("_formatter"))
					{
						String ref = root.getString(combinePath(path, i));
						m_context.addFormatterToPath(ref, createList(path, i-1));
					}
					else if (internalProp.equals("_trigger"))
					{
						String ref = root.getString(combinePath(path, i));
						m_context.addTriggerToPath(ref, createList(path, i-1));
					}
					else if (internalProp.equals("_tags"))
					{
						String key = path[i+1];
						String value = (String) entry.getValue().unwrapped();

						List<String> pathList = createList(path, i - 1);
						Map<String, String> pathTags = m_mappedTags.computeIfAbsent(pathList, (k) -> new HashMap<>());
						pathTags.put(key, value);
					}
					else if (internalProp.equals("_prop"))
					{
						String key = path[i+1];
						String value = (String) entry.getValue().unwrapped();

						List<String> pathList = createList(path, i - 1);
						Map<String, String> pathProps = m_mappedProps.computeIfAbsent(pathList, (k) -> new HashMap<>());
						pathProps.put(key, value);
					}
					else
					{
						throw new ConfigurationException("Unknown configuration element: " + internalProp);
					}
				}
			}
		}
	}


	/**
	 *
	 * @param baseConfig
	 * @param overridesConfig
	 * @return
	 */
	public static MetricConfig parseConfig(String baseConfig, String overridesConfig)
	{
		//todo break up this method so it can be built in parts by unit tests
		MetricsContextImpl context = new MetricsContextImpl();
		MetricConfig ret = new MetricConfig(context);

		Config base = ConfigFactory.parseResources(baseConfig);
		Config overrides = ConfigFactory.parseResources(overridesConfig);

		Config config = overrides.withFallback(base).resolve();

		if (config.hasPath("metrics4j"))
		{
			Config metrics4j = config.getConfig("metrics4j");

			//Parse out the sinks
			Config sinks = config.getConfig("metrics4j.sinks");
			if (sinks != null)
				ret.registerStuff(sinks, context::registerSink);

			Config collectors = config.getConfig("metrics4j.collectors");
			if (collectors != null)
				ret.registerStuff(collectors, context::registerCollector);

			Config formatters = config.getConfig("metrics4j.formatters");
			if (formatters != null)
				ret.registerStuff(formatters, context::registerFormatter);

			Config triggers = config.getConfig("metrics4j.triggers");
			if (triggers != null)
				ret.registerStuff(triggers, context::registerTrigger);

			if (metrics4j.hasPath(DUMP_FILE))
			{
				ret.m_dumpFile = metrics4j.getString(DUMP_FILE);

				ret.m_dumpMetrics = true;
				ret.m_dumpConfig = new HashMap<>();
			}

			Config sources = config.getConfig("metrics4j.sources");
			if (sources != null)
				ret.parseSources(sources);
		}

		return ret;
	}


	/*package*/
	public MetricConfig(MetricsContextImpl context)
	{
		m_context = context;
		m_closeables = new ArrayList<>();
		m_mappedTags = new HashMap<>();
		m_mappedProps = new HashMap<>();
		m_mappedMetricNames = new HashMap<>();


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
		log.debug("Shutdown called for Metrics4j");
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

		if (m_dumpFile != null)
		{
			log.debug("Writing dump file {}", m_dumpFile);
			try
			{
				Map<String, Object> metrics4j = getAdd(m_dumpConfig, "metrics4j");

				//make sure root level configs are there.
				getAdd(metrics4j, "sources");
				getAdd(metrics4j, "sinks");
				getAdd(metrics4j, "collectors");
				getAdd(metrics4j, "formatters");
				getAdd(metrics4j, "triggers");

				String dumpConfigStr = ConfigFactory.parseMap(m_dumpConfig).root()
						.render(ConfigRenderOptions.defaults().setOriginComments(false).setJson(false));
				FileWriter out = new FileWriter(m_dumpFile);
				out.write(dumpConfigStr);
				out.flush();
				out.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public ShutdownHookOverride getShutdownHookOverride()
	{
		m_shutdownOverride = true;
		return MetricConfig.this::shutdown;
	}


	public String getMetricNameForKey(ArgKey key)
	{
		return m_mappedMetricNames.get(key.getConfigPath());
	}


	public MetricsContext getContext()
	{
		return m_context;
	}

	public void setProperties(Properties properties)
	{
		m_properties = properties;
	}


	/**
	 Returns a map of tags that you can modify
	 @param argKey
	 @return
	 */
	public Map<String, String> getTagsForKey(ArgKey argKey)
	{
		return getValuesForKey(argKey, m_mappedTags);
	}

	private Map<String, String> getValuesForKey(ArgKey argKey, Map<List<String>, Map<String, String>> mappedTags)
	{
		Map<String, String> ret = new HashMap<>();
		List<String> configPath = argKey.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));
			Map<String, String> pathTags = mappedTags.getOrDefault(searchPath, new HashMap<>());

			for (String key : pathTags.keySet())
			{
				ret.putIfAbsent(formatValue(key), formatValue(pathTags.get(key)));
			}
		}

		return ret;
	}

	public Map<String, String> getPropsForKey(ArgKey argKey)
	{
		return getValuesForKey(argKey, m_mappedProps);
	}

	public boolean isDumpMetrics()
	{
		return m_dumpMetrics;
	}

	private Map<String, Object> getAdd(Map<String, Object> root, String segment)
	{
		return (Map<String, Object>) root.computeIfAbsent(segment, s -> new HashMap<String, Object>());
	}

	/**
	 Adds a source that will be dumped out on shutdown.@param src
	 @param helpText

	 */
	public void addDumpSource(String src, String helpText)
	{
		Map<String, Object> sources = getAdd(getAdd(m_dumpConfig, "metrics4j"), "sources");

		String[] split = src.split("\\.");

		for (int i = 0; i < split.length; i++)
		{
			sources = getAdd(sources, split[i]);
		}
	}
}
