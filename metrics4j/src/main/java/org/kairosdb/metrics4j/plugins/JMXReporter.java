package org.kairosdb.metrics4j.plugins;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import org.kairosdb.metrics4j.MetricSourceManager;
import org.kairosdb.metrics4j.MetricsContext;
import org.kairosdb.metrics4j.collectors.MetricCollector;
import org.kairosdb.metrics4j.reporting.DoubleValue;
import org.kairosdb.metrics4j.reporting.LongValue;
import org.kairosdb.metrics4j.reporting.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ToString
@EqualsAndHashCode
public class JMXReporter implements Plugin, Closeable, NotificationListener
{
	private static final Logger logger = LoggerFactory.getLogger(JMXReporter.class);
	public static final String JMX_TYPE_PROP = "jmx_type";

	private final Map<ObjectName, List<SourceKey>> m_sourceKeyMap = new HashMap<>();

	private MBeanServer m_server;

	private Map<String, String> m_typeMap = new HashMap<>();
	private List<String> m_classNameAttributes = Collections.emptyList();

	public JMXReporter()
	{
	}

	//used for testing
	public JMXReporter(MBeanServer server)
	{
		m_server = server;
	}

	public void setTypeMap(Config typeMap)
	{
		for (Map.Entry<String, ConfigValue> entry : typeMap.entrySet())
		{
			logger.debug("ADDING "+entry.getKey()+ " - " + entry.getValue().unwrapped().toString());
			m_typeMap.put(entry.getKey(), entry.getValue().unwrapped().toString());
		}
	}

	public void setClassNameAttributes(List<String> attributes)
	{
		m_classNameAttributes = attributes;
	}

	@Override
	public void init()
	{
		m_server = ManagementFactory.getPlatformMBeanServer();

		//Add notification for future MBeans
		try
		{
			addMBeanNotification();
		}
		catch (InstanceNotFoundException e)
		{
			logger.error("Unable to add bean notification", e);
		}

		//Register all current MBeans
		loadExistingMBeans();
	}

	@Override
	public void close() throws IOException
	{
		try
		{
			m_server.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this);
		}
		catch (Exception e)
		{
			logger.error("Error removing notification listener", e);
		}
	}

	private void loadExistingMBeans()
	{
		for (ObjectInstance queryMBean : m_server.queryMBeans(null, null))
		{
			registerMBean(queryMBean.getObjectName());
		}
	}

	/*package*/ void addMBeanNotification() throws InstanceNotFoundException
	{
		m_server.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, this, null, null);
	}

	private String addTagsToName(String baseName, Map<String, String> tags)
	{
		StringBuilder ret = new StringBuilder();
		ret.append(baseName);

		for (String attribute : m_classNameAttributes)
		{
			if (tags.containsKey(attribute))
			{
				ret.append(".").append(tags.remove(attribute));
			}
		}

		return ret.toString();
	}

	private void registerMBean(ObjectName beanName)
	{
		if (m_sourceKeyMap.containsKey(beanName))
			return;
		
		List<SourceKey> sourceKeys = new ArrayList<>();
		try
		{
			for (MBeanAttributeInfo attribute : m_server.getMBeanInfo(beanName).getAttributes())
			{
				//This identifies metrics
				if (attribute.isReadable() && !attribute.isWritable())
				{
					Map<String, String> tags = new LinkedHashMap<>(beanName.getKeyPropertyList());

					String type = attribute.getType();
					//Change type if user defined a mapping
					type = m_typeMap.getOrDefault(type, type);

					String className = addTagsToName(beanName.getDomain(), tags);
					String methodName = attribute.getName();

					Map<String, String> sourceProps = MetricSourceManager.getSourceProps(className, methodName);
					type = sourceProps.getOrDefault(JMX_TYPE_PROP, type);

					String helpText = null;
					if (!tags.isEmpty())
						helpText = "tags:"+tags.keySet().stream().collect(Collectors.joining(","));

					if (type.equals("int"))
					{
						MetricSourceManager.addSource(className, methodName,
								tags, helpText, new IntAttributeSource(beanName, attribute.getName()));
						sourceKeys.add(new SourceKey(className, methodName, tags));
					}
					else if (type.equals("long"))
					{
						MetricSourceManager.addSource(className, methodName,
								tags, helpText, new LongAttributeSource(beanName, attribute.getName()));
						sourceKeys.add(new SourceKey(className, methodName, tags));
					}
					else if (type.equals("float"))
					{
						MetricSourceManager.addSource(className, methodName,
								tags, helpText, new FloatAttributeSource(beanName, attribute.getName()));
						sourceKeys.add(new SourceKey(className, methodName, tags));
					}
					else if (type.equals("double"))
					{
						MetricSourceManager.addSource(className, methodName,
								tags, helpText, new DoubleAttributeSource(beanName, attribute.getName()));
						sourceKeys.add(new SourceKey(className, methodName, tags));
					}
					else if (type.equals("javax.management.openmbean.CompositeData"))
					{
						MetricSourceManager.addSource(className, methodName,
								tags, helpText, new CompositeAttributeSource(beanName, attribute.getName()));
						sourceKeys.add(new SourceKey(className, methodName, tags));
					}
					else
					{
						logger.debug("UNKNOWN TYPE: "+type + " for class "+className);
					}

				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		if (sourceKeys.size() != 0)
			m_sourceKeyMap.put(beanName, sourceKeys);
	}

	private void unregisterMBean(ObjectName beanName)
	{
		try
		{
			List<SourceKey> sourceKeys = m_sourceKeyMap.remove(beanName);

			if (sourceKeys != null)
			{
				for (SourceKey sourceKey : sourceKeys)
				{
					MetricSourceManager.removeSource(sourceKey.getClassName(), sourceKey.getMethodName(),
							sourceKey.getTags());
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void handleNotification(Notification notification, Object handback)
	{
		if (!(notification instanceof MBeanServerNotification)) {
			logger.debug("Ignored notification of class " + notification.getClass().getName());
			return;
		}
		MBeanServerNotification mbsn = (MBeanServerNotification) notification;
		String what = "";
		ObjectName beanName = mbsn.getMBeanName();
		if (notification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION))
		{
			registerMBean(beanName);
			what = "MBean registered";
		}
		else if (notification.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION))
		{
			unregisterMBean(beanName);
			what = "MBean unregistered";
		}

		logger.debug("Received MBean Server notification: {}: {}", what, beanName);
	}

	private static void reportValue(MetricReporter reporter, String key, Long value)
	{
		if (value != null)
			reporter.put(key, new LongValue(value));
	}

	private static void reportValue(MetricReporter reporter, String key, Integer value)
	{
		if (value != null)
			reporter.put(key, new LongValue(value));
	}

	private static void reportValue(MetricReporter reporter, String key, Float value)
	{
		if (value != null && !value.isInfinite() && !value.isNaN())
			reporter.put(key, new DoubleValue(value));
	}

	private static void reportValue(MetricReporter reporter, String key, Double value)
	{
		if (value != null && !value.isInfinite() && !value.isNaN())
			reporter.put(key, new DoubleValue(value));
	}

	private abstract class AttributeSource implements MetricCollector
	{
		protected final ObjectName m_objectName;
		protected final String m_attribute;

		private AttributeSource(ObjectName objectName, String attribute)
		{
			m_objectName = objectName;
			m_attribute = attribute;
		}
	}


	private class IntAttributeSource extends AttributeSource
	{
		private IntAttributeSource(ObjectName objectName, String attribute)
		{
			super(objectName, attribute);
		}

		@Override
		public void reportMetric(MetricReporter metricReporter)
		{
			Integer value = null;

			try
			{
				value = (Integer)m_server.getAttribute(m_objectName, m_attribute);
			}
			catch (Exception e)
			{
				logger.debug("Failed to read JMX attribute "+m_objectName+": "+m_attribute, e);
			}

			reportValue(metricReporter, "value", value);
		}

	}

	private class LongAttributeSource extends AttributeSource
	{
		private LongAttributeSource(ObjectName objectName, String attribute)
		{
			super(objectName, attribute);
		}

		@Override
		public void reportMetric(MetricReporter metricReporter)
		{
			Long value = null;

			try
			{
				value = (Long)m_server.getAttribute(m_objectName, m_attribute);
			}
			catch (Exception e)
			{
				logger.debug("Failed to read JMX attribute "+m_objectName+": "+m_attribute, e);
			}

			reportValue(metricReporter, "value", value);
		}
	}

	private class FloatAttributeSource extends AttributeSource
	{

		private FloatAttributeSource(ObjectName objectName, String attribute)
		{
			super(objectName, attribute);
		}

		@Override
		public void reportMetric(MetricReporter metricReporter)
		{
			Float value = null;

			try
			{
				value = (Float)m_server.getAttribute(m_objectName, m_attribute);
			}
			catch (Exception e)
			{
				logger.debug("Failed to read JMX attribute "+m_objectName+": "+m_attribute, e);
			}

			reportValue(metricReporter, "value", value);
		}
	}

	private class DoubleAttributeSource extends AttributeSource
	{

		private DoubleAttributeSource(ObjectName objectName, String attribute)
		{
			super(objectName, attribute);
		}

		@Override
		public void reportMetric(MetricReporter metricReporter)
		{
			Double value = null;

			try
			{
				value = (Double)m_server.getAttribute(m_objectName, m_attribute);
			}
			catch (Exception e)
			{
				logger.debug("Failed to read JMX attribute "+m_objectName+": "+m_attribute, e);
			}

			reportValue(metricReporter, "value", value);
		}
	}

	private class CompositeAttributeSource extends AttributeSource
	{
		private CompositeAttributeSource(ObjectName objectName, String attribute)
		{
			super(objectName, attribute);
		}

		@Override
		public void reportMetric(MetricReporter metricReporter)
		{
			try
			{
				CompositeData data = (CompositeData) m_server.getAttribute(m_objectName, m_attribute);
				if (data != null)
				{
					CompositeType type = data.getCompositeType();

					for (String key : type.keySet())
					{
						OpenType<?> openType = type.getType(key);
						if (openType == SimpleType.LONG)
						{
							reportValue(metricReporter, key, (Long)data.get(key));
						}
						else if (openType == SimpleType.INTEGER)
						{
							reportValue(metricReporter, key, (Integer) data.get(key));
						}
						else if (openType == SimpleType.FLOAT)
						{
							reportValue(metricReporter, key, (Float)data.get(key));
						}
						else if (openType == SimpleType.DOUBLE)
						{
							reportValue(metricReporter, key, (Double)data.get(key));
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.debug("Failed to read JMX attribute "+m_objectName+": "+m_attribute, e);
			}
		}
	}
}
