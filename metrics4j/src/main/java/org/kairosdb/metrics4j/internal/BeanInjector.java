package org.kairosdb.metrics4j.internal;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigMemorySize;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import com.typesafe.config.Optional;
import org.kairosdb.metrics4j.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.kairosdb.metrics4j.configuration.MetricConfig.CLASS_PROPERTY;
import static org.kairosdb.metrics4j.configuration.MetricConfig.FOLDER_PROPERTY;

public class BeanInjector
{
	public static final Logger logger = LoggerFactory.getLogger(BeanInjector.class);
	private final String m_objName;
	private final Class<?> m_class;
	private final Map<String, PropertyDescriptor> m_propMap = new HashMap<>();

	public BeanInjector(String objName, Class<?> objClass) throws IntrospectionException
	{
		m_objName = objName;
		m_class = objClass;
		BeanInfo beanInfo = Introspector.getBeanInfo(objClass);
		PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

		for (PropertyDescriptor propertyDescriptor : propertyDescriptors)
		{
			m_propMap.put(getDashPropertyName(propertyDescriptor.getName()), propertyDescriptor);
		}
	}

	private String getDashPropertyName(String camelCaseName)
	{
		StringBuilder sb = new StringBuilder();

		for (char c : camelCaseName.toCharArray())
		{
			if (Character.isUpperCase(c))
				sb.append('-').append(Character.toLowerCase(c));
			else
				sb.append(c);
		}

		return (sb.toString());
	}

	public Object createInstance(Config config)
	{
		Object instance;
		try
		{
			instance = m_class.newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			String msg = "Failed to instantiate instance of '"+m_class.getName()+
					"' for object '"+m_objName+"'.  Error: "+e.getMessage();

			throw new ConfigurationException(msg);
		}

		Set<Map.Entry<String, ConfigValue>> props = config.root().entrySet();
		for (Map.Entry<String, ConfigValue> prop : props)
		{
			String property = prop.getKey();
			//Skip internal
			if (property.equals(CLASS_PROPERTY) || property.equals(FOLDER_PROPERTY))
				continue;

			PropertyDescriptor pd = m_propMap.get(property);

			if (pd == null || pd.getWriteMethod() == null)
			{
				String msg = "Property '" + property + "' was specified for object '" + m_objName +
						"' but no matching setter was found on '" + m_class.getName() + "'";

				throw new ConfigurationException(msg);
			}

			Method method = pd.getWriteMethod();

			//This type contains the parameters if it is a List<String>
			Type propClass = method.getGenericParameterTypes()[0];

			Object propValue = getValue(propClass, config, property);
			if (propValue == null)
			{
				String msg = "Object type for '"+method.getName()+"' on '"+m_class+"' is of un unsupported type.  Supported types are (int, long, double, boolean, String, Duration and Enum)";

				throw new ConfigurationException(msg);
			}
			/*try
			{
				propValue = m_gson.fromJson(prop.getValue(), propClass);
				validateObject(propValue, context + "." + property);
			}
			catch (ContextualJsonSyntaxException e)
			{
				throw new BeanValidationException(new SimpleConstraintViolation(e.getContext(), e.getMessage()), context);
			}
			catch (NumberFormatException e)
			{
				throw new BeanValidationException(new SimpleConstraintViolation(property, e.getMessage()), context);
			}*/



			try
			{
				method.invoke(instance, propValue);
			}
			catch (Exception e)
			{
				logger.error("Invocation error: ", e);
				String msg = "Call to " + m_class.getName() + ":" + method.getName() +
						" failed with message: " + e.getMessage();

				throw new ConfigurationException(msg);
			}
		}

		return instance;
	}


	private static Object getValue(Type parameterType, Config config, String configPropName)
	{
		try
		{
			Type compareType = parameterType;

			//This will convert a List<String> to just a List for comparison
			if (parameterType instanceof ParameterizedType)
				compareType = ((ParameterizedType) parameterType).getRawType();

			if (compareType == Boolean.class || compareType == boolean.class)
			{
				return config.getBoolean(configPropName);
			}
			else if (compareType == Integer.class || compareType == int.class)
			{
				return config.getInt(configPropName);
			}
			else if (compareType == Double.class || compareType == double.class)
			{
				return config.getDouble(configPropName);
			}
			else if (compareType == Long.class || compareType == long.class)
			{
				return config.getLong(configPropName);
			}
			else if (compareType == String.class)
			{
				return config.getString(configPropName);
			}
			else if (compareType == Duration.class)
			{
				return config.getDuration(configPropName);
			}
			else if (compareType == Config.class)
			{
				return config.getConfig(configPropName);
			}
			else if (compareType.equals(List.class))
			{
				return getListValue(parameterType, config, configPropName);
			}
			else if (compareType == Set.class)
			{
				return getSetValue(parameterType, config, configPropName);
			}
			else if (Enum.class.isAssignableFrom((Class<?>) compareType))
			{
				return Enum.valueOf((Class<Enum>) parameterType, config.getString(configPropName));
			}
			else
				return null;
		}
		catch (Exception e)
		{
			logger.error("Failed to get value for property "+configPropName, e);
			return null;
		}
	}

	private static Object getSetValue(Type parameterType, Config config, String configPropName) {
		return new HashSet((List) getListValue(parameterType, config, configPropName));
	}

	private static Object getListValue(Type parameterType, Config config, String configPropName) {
		Type elementType = ((ParameterizedType)parameterType).getActualTypeArguments()[0];

		if (elementType == Boolean.class) {
			return config.getBooleanList(configPropName);
		} else if (elementType == Integer.class) {
			return config.getIntList(configPropName);
		} else if (elementType == Double.class) {
			return config.getDoubleList(configPropName);
		} else if (elementType == Long.class) {
			return config.getLongList(configPropName);
		} else if (elementType == String.class) {
			return config.getStringList(configPropName);
		} else if (elementType == Duration.class) {
			return config.getDurationList(configPropName);
		} else if (((Class<?>) elementType).isEnum()) {
			@SuppressWarnings("unchecked")
			List<Enum> enumValues = config.getEnumList((Class<Enum>) elementType, configPropName);
			return enumValues;
		} /*else if (hasAtLeastOneBeanProperty((Class<?>) elementType)) {
			List<Object> beanList = new ArrayList<Object>();
			List<? extends Config> configList = config.getConfigList(configPropName);
			for (Config listMember : configList) {
				beanList.add(createInternal(listMember, (Class<?>) elementType));
			}
			return beanList;
		} */else {
			return null;
		}
	}


}
