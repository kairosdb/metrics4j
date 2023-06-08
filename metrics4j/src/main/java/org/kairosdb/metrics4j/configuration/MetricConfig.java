package org.kairosdb.metrics4j.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.PostConfig;
import org.kairosdb.metrics4j.PostConstruct;
import org.kairosdb.metrics4j.internal.ArgKey;
import org.kairosdb.metrics4j.internal.BeanInjector;
import org.kairosdb.metrics4j.internal.MetricsContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.beans.IntrospectionException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetricConfig
{
	public static final String CLASS_PROPERTY = "_class";
	public static final String FOLDER_PROPERTY = "_folder";
	public static final String DUMP_FILE = "_dump-file";
	public static final String METRIC_NAME = "_metric-name";

	public static final String CONFIG_SYSTEM_PROPERTY = "METRICS4J_CONFIG";
	public static final String OVERRIDES_SYSTEM_PROPERTY = "METRICS4J_OVERRIDES";

	public static final String PATH_SPLITTER_REGEX = "[\\.\\$]";

	private static final Logger log = LoggerFactory.getLogger(MetricConfig.class);

	private static final Pattern formatPattern = Pattern.compile("\\%\\{([^\\}]*)\\}");

	private Properties m_properties = new Properties();

	private final Map<List<String>, Map<String, String>> m_mappedTags;
	private final Map<List<String>, Map<String, String>> m_mappedProps;
	private final Map<List<String>, String> m_mappedMetricNames;
	private final Map<List<String>, Boolean> m_disabledPaths;

	private final MetricsContextImpl m_context;
	private final List<Closeable> m_closeables;

	private boolean m_shutdownOverride = false;
	private boolean m_dumpMetrics = false;
	private String m_dumpFile;
	private Map<String, Object> m_dumpConfig;
	private final List<PostConstruct> m_postConstructs;
	private final List<PostConfig> m_postConfigs;

	private Config m_config;


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
			keys.add(config.getKey().split(PATH_SPLITTER_REGEX)[0]);
		}

		for (String name : keys)
		{

			T classInstance = loadClass(configs.getConfig(name), name);

			register.accept(name, classInstance);

			if (classInstance instanceof PostConstruct)
			{
				m_postConstructs.add((PostConstruct)classInstance);
			}

			if (classInstance instanceof PostConfig)
			{
				m_postConfigs.add((PostConfig)classInstance);
			}

			if (classInstance instanceof Closeable)
			{
				m_closeables.add((Closeable)classInstance);
			}
		}
	}

	/*package*/ static List<String> appendSourceName(List<String> parent, String child)
	{
		List<String> copy = new ArrayList<>(parent);

		String[] splitNames = child.split(PATH_SPLITTER_REGEX);

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
			String[] path = entry.getKey().split(PATH_SPLITTER_REGEX);

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
						//sink can be either a single string or a list of string
						String sinkPath = combinePath(path, i);

						ConfigValueType sinkValueType = root.getValue(sinkPath).valueType();
						if (sinkValueType == ConfigValueType.STRING)
						{
							String ref = root.getString(sinkPath);
							m_context.addSinkToPath(ref, createList(path, i-1));
						}
						else if (sinkValueType == ConfigValueType.LIST)
						{
							List<String> sinkList = root.getStringList(sinkPath);
							for (String sink : sinkList)
							{
								m_context.addSinkToPath(sink, createList(path, i-1));
							}
						}

					}
					else if (internalProp.equals("_collector"))
					{
						//collector can be either a single string or a list of string
						String collectorPath = combinePath(path, i);

						ConfigValueType collectorValueType = root.getValue(collectorPath).valueType();
						if (collectorValueType == ConfigValueType.STRING)
						{
							String ref = root.getString(collectorPath);
							m_context.addCollectorToPath(ref, createList(path, i-1));
						}
						else if (collectorValueType == ConfigValueType.LIST)
						{
							List<String> collectorList = root.getStringList(collectorPath);
							for (String collector : collectorList)
							{
								m_context.addCollectorToPath(collector, createList(path, i-1));
							}
						}
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
					else if (internalProp.equals("_disabled"))
					{
						Boolean value = (Boolean)entry.getValue().unwrapped();
						m_disabledPaths.put(createList(path, i - 1), value);
					}
					else
					{
						throw new ConfigurationException("Unknown configuration element: " + internalProp);
					}
				}
			}
		}
	}

	private static void registerIfNotNull(Config config, String path, Consumer<Config> register)
	{
		if (config.hasPath(path))
			register.accept(config.getConfig(path));
	}

	/**
	 *
	 * @param baseConfig
	 * @param overridesConfig
	 * @return
	 */
	public static MetricConfig parseConfig(String baseConfig, String overridesConfig)
	{
		MetricsContextImpl context = new MetricsContextImpl();
		MetricConfig ret = new MetricConfig(context);

		String configFilePath = System.getProperty(CONFIG_SYSTEM_PROPERTY);
		String overridesFile = System.getProperty(OVERRIDES_SYSTEM_PROPERTY);

		Config base, overrides;

		if (configFilePath != null)
		{
			log.info("Loading metrics4j config file: "+configFilePath);
			File configFile = new File(configFilePath);
			if (!configFile.exists())
				log.info("Unable to locate file: "+configFilePath);

			base = ConfigFactory.parseFile(configFile);
		}
		else
		{
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if (contextClassLoader.getResource(baseConfig) == null)
				log.info("Unable to locate " + baseConfig + " are you sure it is in the classpath?");

			base = ConfigFactory.parseResources(baseConfig);
		}

		if (overridesFile != null)
		{
			overrides = ConfigFactory.parseFile(new File(overridesFile));
		}
		else
		{
			overrides = ConfigFactory.parseResources(overridesConfig);
		}

		Config config = overrides.withFallback(base);

		//Load system properties
		Config sysConfig = ConfigFactory.systemProperties();
		config = sysConfig.withFallback(config);

		//Get environment variables
		config = applyEnvironmentVariables(config);


		config = config.resolve();
		ret.m_config = config;

		if (config.hasPath("metrics4j"))
		{
			Config metrics4j = config.getConfig("metrics4j");

			registerIfNotNull(config, "metrics4j.plugins", (plugins) -> ret.registerStuff(plugins, context::registerPlugin));
			registerIfNotNull(config, "metrics4j.sinks", (sinks) -> ret.registerStuff(sinks, context::registerSink));
			registerIfNotNull(config, "metrics4j.collectors", (collectors) -> ret.registerStuff(collectors, context::registerCollector));
			registerIfNotNull(config, "metrics4j.formatters", (formatters) -> ret.registerStuff(formatters, context::registerFormatter));
			registerIfNotNull(config, "metrics4j.triggers", (triggers) -> ret.registerStuff(triggers, context::registerTrigger));

			for (PostConstruct postConstruct : ret.m_postConstructs)
			{
				postConstruct.init(context);
			}

			if (metrics4j.hasPath(DUMP_FILE))
			{
				ret.m_dumpFile = metrics4j.getString(DUMP_FILE);

				ret.m_dumpMetrics = true;
				ret.m_dumpConfig = new ConcurrentHashMap<>();

				Thread dumpThread = new Thread(() -> {
					try
					{
						Thread.sleep(60000);
					}
					catch (InterruptedException e) { }
					ret.dumpConfFile();
				});
				dumpThread.setDaemon(true);
				dumpThread.start();
			}

			registerIfNotNull(config, "metrics4j.sources", (sources) -> ret.parseSources(sources));
		}

		return ret;
	}

	protected static String toEnvVarName(String propName) {
		return propName.toUpperCase().replace('.', '_');
	}

	/*
	 * allow overwriting any existing property via correctly named environment variable
	 * e.g. kairosdb.datastore.cassandra.host_list via KAIROSDB_DATASTORE_CASSANDRA_HOST_LIST
	 */
	protected static Config applyEnvironmentVariables(Config config)
	{
		Map<String, String> env = System.getenv();
		Map<String, String> props = new HashMap<>();
		for (Map.Entry<String, ConfigValue> propName : config.entrySet())
		{
			String envVarName = toEnvVarName(propName.getKey());
			if (env.containsKey(envVarName))
			{
				props.put(propName.getKey(), env.get(envVarName));
			}
		}

		Config envConfig = ConfigFactory.parseMap(props);
		return envConfig.withFallback(config);
	}


	/*package*/
	public MetricConfig(MetricsContextImpl context)
	{
		m_context = context;
		m_closeables = new ArrayList<>();
		m_mappedTags = new HashMap<>();
		m_mappedProps = new HashMap<>();
		m_mappedMetricNames = new HashMap<>();
		m_disabledPaths = new HashMap<>();
		m_postConstructs = new ArrayList<>();
		m_postConfigs = new ArrayList<>();


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

	public void runPostConfigInit()
	{
		for (PostConfig postConfig : m_postConfigs)
		{
			postConfig.init();
		}
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
				log.error("Error closing " + closeable.getClass().getName(), e);
			}
		}
		dumpConfFile();
	}

	private void dumpConfFile()
	{
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

	public boolean isDisabled(ArgKey argKey)
	{
		List<String> configPath = argKey.getConfigPath();
		for (int i = configPath.size(); i >= 0; i--)
		{
			List<String> searchPath = new ArrayList<>(configPath.subList(0, i));

			Boolean disabled = m_disabledPaths.get(searchPath);
			if (disabled != null)
			{
				return disabled;
			}
		}

		return false;
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

	/**
	 returns immutable map of properties for the specified key context
	 @param argKey
	 @return
	 */
	public Map<String, String> getPropsForKey(ArgKey argKey)
	{
		return Collections.unmodifiableMap(getValuesForKey(argKey, m_mappedProps));
	}

	public boolean isDumpMetrics()
	{
		return m_dumpMetrics;
	}

	private Map<String, Object> getAdd(Map<String, Object> root, String segment)
	{
		return (Map<String, Object>) root.computeIfAbsent(segment, s -> new ConcurrentHashMap<>());
	}

	/**
	 Adds a source that will be dumped out on shutdown.@param src
	 @param helpText

	 */
	public void addDumpSource(String src, String helpText)
	{
		Map<String, Object> sources = getAdd(getAdd(m_dumpConfig, "metrics4j"), "sources");

		String[] split = src.split(PATH_SPLITTER_REGEX);

		for (int i = 0; i < split.length; i++)
		{
			sources = getAdd(sources, split[i]);
		}



		if (helpText != null)
			sources.put("_help", helpText);
	}

	/**
	 Returns any configuration that was used in initializing metrics4j
	 @param config
	 @return String representation of value
	 */
	public String getConfigString(String config)
	{
		return m_config.getString(config);
	}

	/**
	 Returns any configuration that was used in initializing metrics4j
	 @param config
	 @return ConfigValue object you can unwrap
	 */
	public ConfigValue getConfigValue(String config)
	{
		return m_config.getValue(config);
	}
}
