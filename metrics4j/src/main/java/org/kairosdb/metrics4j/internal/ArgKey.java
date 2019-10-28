package org.kairosdb.metrics4j.internal;

import org.kairosdb.metrics4j.annotation.Key;
import org.kairosdb.metrics4j.configuration.ConfigurationException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Used to put an argument list (ie Object[]) as a key to a hashmap
 */
public interface ArgKey
{
	List<String> getConfigPath();

	String getMethodName();

	String getClassName();
}
