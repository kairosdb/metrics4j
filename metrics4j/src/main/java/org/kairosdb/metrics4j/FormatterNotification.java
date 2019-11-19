package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.formatters.Formatter;

public interface FormatterNotification
{
	void newFormatter(String name, Formatter formatter);
}
