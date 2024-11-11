package org.kairosdb.metrics4j.util;

public class NameParser {
    public static String parseSimpleClassName(String className)
    {
        int i = className.lastIndexOf('.');
        if (i != -1)
            return className.substring(i+1);
        else
            return className;
    }
}
