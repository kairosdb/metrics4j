package org.kairosdb.metrics4j.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 For exported metric classes that have @Reported annotations.  An exported object
 can annotate a single no argument method with this annotation and it will be
 called immediately prior to calling the @Reported methods
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Snapshot
{
}
