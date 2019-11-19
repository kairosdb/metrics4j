package org.kairosdb.metrics4j;

import org.kairosdb.metrics4j.triggers.Trigger;

public interface TriggerNotification
{
	void newTrigger(String name, Trigger trigger);
}
