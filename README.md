- [metrics4j](#metrics4j)
      - [Have you ever wanted to ...](#have-you-ever-wanted-to-)
  * [Philosophy of using Metrics4j](#philosophy-of-using-metrics4j)
    + [Application Developer](#application-developer)
    + [IT Administrator](#it-administrator)
- [Section 1 (Developer)](#section-1--developer-)
  * [Using the library](#using-the-library)
  * [Different ways to report metrics](#different-ways-to-report-metrics)
  * [Testing with the library](#testing-with-the-library)
- [Section 2 (Admin)](#section-2--admin-)
  * [Configuration](#configuration)
      - [Configuration Parameters](#configuration-parameters)
    + [Sources](#sources)
          + [Overrides](#overrides)
          + [Metric Name](#metric-name)
          + [Tags](#tags)
          + [Props](#props)
        * [Getting available sources](#getting-available-sources)
        * [Disabling sources](#disabling-sources)
    + [Sinks](#sinks)
      - [Slf4JMetricSink](#slf4jmetricsink)
      - [TelnetSink](#telnetsink)
      - [GraphitePlaintextSink](#graphiteplaintextsink)
      - [InfluxSink](#influxsink)
      - [PrometheusSink](#prometheussink)
      - [KairosSink](#kairossink)
      - [TimescaleDBSink](#timescaledbsink)
      - [StatsDTCPSink](#statsdtcpsink)
    + [Collectors](#collectors)
      - [DoubleCounter](#doublecounter)
      - [LongCounter](#longcounter)
      - [LongGauge](#longgauge)
      - [MaxLongGauge](#maxlonggauge)
      - [DoubleGauge](#doublegauge)
      - [SimpleStats](#simplestats)
      - [SimpleTimerMetric](#simpletimermetric)
      - [StringReporter](#stringreporter)
    + [Formatters](#formatters)
      - [TemplateFormatter](#templateformatter)
    + [Triggers](#triggers)
      - [IntervalTrigger](#intervaltrigger)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>


# metrics4j
Library for abstracting the reporting of metrics in your code from sending them to a time series service.

This library is still in development, keep checking back as progress is moving quickly

#### Have you ever wanted to ...
 * change how often an application reports metrics
 * change how a metric is reported (rate vs counter)
 * change the name of a metric
 * turn a metric off or on 
 * report a metric to more than one timeseries backend
 * have an application report to something besides prometheus
 
All of the above on an application already deployed in production?  Then this library is for your 
(or the developers that wrote the application)

## Philosophy of using Metrics4j
The metrics4j library is designed to separate the role of the application developer
from the IT administrator when it comes to reporting metrics.  When, how often and
where metrics are reported is not the job of the developer.


### Application Developer
The application developer's role is to identify interesting metrics and report them.
The Metrics4j library lets the developer report numbers or durations 
with a clean, easy to use api.  The interpretation of those numbers, ie. is it a counter
or a rate, is determined at runtime by the administrator along with where to send and how 
often to send.

### IT Administrator
The IT Admin is the one that deploys the application and is able, by configuration at runtime,
to determine the following
1. What metrics to send
1. How often to send
1. Where to send the metrics
1. Name of the metric and format
1. How to interpret the metric, is it a guage, rate or counter?

All of the above is determined through configuration via the metrics4j.conf file.  In some
cases additional jar files may need to be placed in the classpath depending on the plugins used.

The rest of the documentation is split into 2 sections, the first is for developers
using the library and the second section is for admins trying to configure the library
in a deployed application.

# Section 1 (Developer)

Checkout the short video I did on using Metrics4j in your application: https://youtu.be/9r-NvsIezUc

## Using the library
Anyone wanting to instrument their code will only have to do three things.
1.  Create an interface that describes the metric to report.
1.  Use the MetricSourceManager to instantiate an instance of the above interface.
1.  Call the method to report metrics.

Actual reporting of the metrics and where they are reporting to will be determined 
at run time based on a configuration file. (covered in section 2)

Each metric reported consists of a value (long, double, duration or string) a timestamp (determined
by when the metric is reported) and a set of tags (key value pairs).

Lets look at an example of how this would be done.  Lets say you have a service
that receives messages and you want to report the amount of data your service is
receiving.  First step is to create an interface in your code that defines what 
you are reporting:
```java
public interface MessageSizeReporter
   {
   LongCollector reportSize(@Key("host") String host);
   DurationCollector reportTime(@Key("host") String host);
   }
```

Notice the `@Key("host")` annotation on the host parameter.  This lets metrics4j 
know that you want to specify the host tag each time this is called, in this way
you separate the metrics for each host.

While the parameters to a reporting method are usually strings they can be any type,
Metrics4j will call .toString() on the parameter so it can be passed as a string
tag to the timeseries backend.

**Note:** Not all timeseries backends support tags.  In those cases you will need to include 
the tag as part of the metric name using a formatter (see below)

The return type for the method indicates what type of value will be collected.
Our example collects long values and durations (only return the *Collector interfaces).

You can have as many methods on your interface as you like, in our example we are 
also reporting the time it took to process the message.

In the class where you want to report your metric you will get an instance
of `MessageSizeReporter` from the MetricSourceManager static method 
(much like getting a logger from slf4j)

```java
public static MessageSizeReporter reporter = MetricSourceManager.getSource(MessageSizeReporter.class);
```

In your code you will call the `reportSize` method and pass the size
of the message on to the LongCollector that is returned.

```java
reporter.reportSize(host).put(messageSize);
```

That's it.  You may be wondering what your metric name will look like?  Well 
that isn't up to you, it's up to whomever configures and runs your software.
Both the interface name and the method name are available for formatting the metric
name so it is a good idea to name them something appropriate.  You can also annotate
your methods with `@Help("good help text here")` to give your users clues as to 
what the metric is.

## Different ways to report metrics

todo:

Put values into a collector

Duration helper methods

Custom MetricCollector


## Testing with the library
As any good developer will do you will want to test your code to make sure
it reports metrics.
The following example is using mockito but any mock library will work.

```java
//mock the collector and register it with ReporterFactory for a specific call
LongCollector myCollector = mock(LongCollector.class);
MetricSourceManager.setCollectorForSource(myCollector, MessageSizeReporter.class).reportSize("localhost");
```

First create a mock LongCollector object.  Then we register the mock with the MetricSourceManager
for when `MessageSizeReporter.reportSize` method is called with the host equal to
'localhost'.  This lets us be very specific as to when metrics4j is to use our mock
object.  At the end of our test we can verify that our mock was called with specific 
parameters

```java
verify(myCounter).put(42);
```

# Section 2 (Admin)

Metrics4j is designed to let you, the admin, determine for each metric being reported
1. Whether or not the metric is reported.
1. The name of the metric, by specifying a formatter for the metric.
1. How often metrics are reported.
1. How to aggregate metric data while waiting to be reported (by specifying what Collector to use)
1. Where to send the metrics - KairosDB, InfluxDB, etc..

(The following is subject to change as we work through the beta)

When Metrics4j loads it will try to find two files named metrics4j.conf and metrics4j.properties in the classpath.
If neither file is found, all the reporting methods are effectively no-ops.

The location of the above files can also be specified using java system properties
METRICS4J_CONFIG and METRICS4J_OVERRIDES.  Example: -DMETRICS4J_CONFIG=/etc/metrics4j.conf

What is a .conf file?  Metrics4j has the Hocon library from LightBend shaded into the jar (https://github.com/lightbend/config)
Hocon is a human readable json format that is awesome.  We will only cover the basics here, it is worth
your time to review their documentation as they have some cool features.

Metrics4j loads up the .conf file and then uses the .properties as overrides.  Most 
configuration management systems can generate .properties files so it is easier to 
place situation aware variables within the metrics4j.properties file and then reference them
from the metrics4j.conf.
 
## Configuration
The top level of the configuration file looks like this
```hocon
metrics4j: {
  sources: { 
    #Defines sources and associates sinks, collectors, formatters and triggers with them
    #Sources are typically places in code that reports metrics.
    }
    
  sinks: {
    #Defines what sinks to use - where to send the metrics
  }
    
  collectors: {
    #Defines what collectors will be used and configures them
  }
    
  formatters: {
    #Can reformat metric names when reporting
  }
    
  triggers: {
    #determines when to collect metrics
  }
}
```

Collectors are cloned for each source, Sinks, Formatters and Triggers are treated
as singletons.

Metrics4j uses bean property injection when loading plugins.  The properties should be
dash delimited in the hocon file, so if your sink has a `setHostName(String name)` method
you will set `host-name: ""` in the conf file and it will get injected after the plugin
is loaded but before `init()` is called.

In some cases you may have a plugin that has conflicting dependencies with 
the main application that Metrics4j is being used in.  In this case you can 
use the plugin feature of Metrics4j by specifying a _folder attribute on the 
component you are including.  The _folder attribute defines a location to find jar
files that contain the component (collector, sink, etc).  These jars are loaded
in a separate class loader and isolated to prevent conflicts.

#### Configuration Parameters

For any attribute or element value you can insert a parameter surrounded by ${ } that
will be replaced by either a properties value or an environment value (this is the Hocon substitution feature).

### Sources
The purpose of sources is to associate a sink/collector/formatter/trigger with 
the various sources of metrics throughout the application.

Lets look at the previous example of MessageSizeReporter and I want to set reportSize() 
to use a counter, this is how that would look (assuming MessageSizeReporter was in package foo.com):

```hocon
metrics4j: {
  sources: {
    com.foo.MessageSizeReporter.reportSize: {
      _collector: myCounter
    }
  }
  collectors: {
    myCounter: {
      _class: "org.kairosdb.metrics4j.collectors.impl.LongCounter"
      reset: true
    }
  }
}
```

When MessageSizeReporter is created metrics4j will search up the tree looking for a
defined LongCollector for reportSize to return.  If I knew I wanted all collectors
to be the same then I could reference the collector at the root once and all would
use it.  The above collector is configured to reset its value after it is reported.

When using Hocon the '.' is the same as a nested object so the following configurations
are the same

```hocon
metrics4j: {
  sources: {
    com.foo.MessageSizeReporter.reportSize: {
      _collector: myCounter
    }
  }
}
metrics4j: {
  sources: {
    com: {
      foo: {
        MessageSizeReporter.reportSize: {
          _collector: myCounter
        }
      }
    }
  }
}
```

Defaults can be specified at the root of sources and then overridden at any level
that is convenient for you.

For each source (ie. reportSize()) you can define a collector, a trigger, a formatter and 
one or more sinks.

In order to get a metric to report you must define a `_collector`, a `_sink` and a `_trigger` 
reference.  The values of `_collector` and `_sink` can be either a string value or a list 
of strings if you want to reference more than one at the same level.

###### Overrides

Just a quick note on overriding values using Hocon.  Lets say in the above example I want
to configure the reset option of myCounter using configuration management.  If I use the 
metrics4j.properties file I can do this in one of two ways.

Override

In this case I replace the value using the .properties like so
```properties
metrics4j.collectors.myCounter.rest=false
```

Substitution

In the .conf file I replace the value of rest with ${reset-option} and then my .properties file looks
like this
```properties
reset-option=false
```

Technically speaking the .properties file is loaded using the .conf file as a fallback and then resolved.

###### Metric Name
You can explicitly call out the metric name for a source using the `_metric-name` attribute.
```hocon
metrics4j: {
sources: {
  org.kairosdb.metrics4j.configuration.TestSource: {
    countSomething: {
      _metric-name: "my_metric.count_something"
      }
    }
  }
}
```
This attribute is then made available to the template format as `%{metricName}`.
It is still a good idea to use a formatter as some collectors have more than one field
they report (ie. min, max, avg) so you need to tell metrics4j how to append that on to
the metric name.

###### Tags
Tags gives you a way to add tags to your metric to be sent off.  The tag element can
be defined at any level under sources.
```hocon
metrics4j: {
  sources: {
    _tags: {
      host: "localhost" #default value
    }
    org.kairosdb: {
      _tags: {
        host: "localhost_override" #override at this context
      }
    }
  }
}
```

The above example sets tags a two different levels and the more nested overrides
those towards the root.

This can also be used to override a noisy tag value in order to reduce the 
cardinality of a particular metric.

###### Props
Props (properties) are a way to pass context information to formatters or sinks about
the metric.  For example you way want to tell the statsd sink that the value is a counter
```hocon
sources: {
  foo.MyClass.myMethod: {
    _props: {
      statsd_type: "c"
    }
  }
}
```

##### Getting available sources

You may have just downloaded a project and are unsure what sources are available
to configure.  You can dump all sources by specifying the _dump-file attribute under the 
metrics4j tag like so:

```hocon
metrics4j: {
  _dump-file: "dump_sources.conf"
}
```
Start the application and let it run for a bit and then shut it down.  Metrics4j
will dump out all the sources it saw while running.  In case the shutdown hook
is not ran metrics4j will also dump the config after 1 minute.

##### Disabling sources

If a source hasn't been configured with a collector it will not report but sometimes
it is easier to disable portions of the source tree.  You can disable any part of the 
source tree by adding a `_disabled: true` at the level you wish to disable.  Disabled 
sources can be overridden by adding `_disabled: false` further down the tree.

### Sinks
A sink defines a destination to send the metrics to.  The following are built in 
sinks.

#### Slf4JMetricSink
Reports metrics to an Slf4j logger.  The log-level attribute controls the log level (DEBUG, WARN, INFO, etc).
```hocon
sinks: {
    slf4j: {
      _class: "org.kairosdb.metrics4j.sinks.Slf4JMetricSink"
      log-level: INFO
    }
}
```

* _log-level:_ (INFO, DEBUG, WARN, ERROR, TRACE), log level to use when reporting metrics

#### TelnetSink

Sends data using the telnet protocol supported by OpenTSDB and KairosDB.

```hocon
sinks: {
  telnet: {
    _class: "org.kairosdb.metrics4j.sinks.TelnetSink"
    host: "localhost"
    port: 4242
    resolution: "SECONDS" #Kairos also supports MILLISECONDS
  }
}
```

The resolution attribute can be either SECONDS or MILLISECONDS sending either a put or putm
respectively

* _host:_ Host to connect to
* _port:_ Port to use
* _protocol:_ (UDP/**TCP**) Protocol to use
* _max-udp-packet-size:_ (**1024**) Max packet size when using UDP
* _resolution:_ (SECONDS/**MILLISECONDS**) If set to SECONDS this sink will use the 'put' command
if set to MILLISECONDS the sink will use the 'putm' command


#### GraphitePlaintextSink

Sends data using the plaintext protocol.  It takes three attributes for host, port
and whether to include tags.

```hocon
sinks: {
  graphite: {
    _class: "org.kairosdb.metrics4j.sinks.GraphitePlaintextSink"
    host: "localhost"
    port: 2003
    include-tags: true #newer graphite versions support tags
  }
}
```

* _include-tags:_ includes tags for newer graphite version
* _host:_ Host to connect to
* _port:_ Port to use
* _protocol:_ (UDP/**TCP**) Protocol to use
* _max-udp-packet-size:_ (**1024**) Max packet size when using UDP

#### InfluxSink

The Influx sink is a separate jar that needs to be placed in the classpath along 
with its dependencies (it includes the apache httpclient).

```hocon
sinks: {
  influx: {
    _class: "org.kairosdb.metrics4jplugin.influxdb.InfluxSink"
    host-url: "http://localhost:8086/write?db=mydb"
  }
}
```

* _host-url:_ url endpoint for influx

#### PrometheusSink

The Prometheus sink is a separate jar that needs to be placed in the classpath along
with its dependencies (it includes the prometheus simpleclient and simpleclient_httpserver).

Prometheus requires both a sink and a trigger to be defined.  They both need to be referenced
in the sources section as well.

```hocon
sinks: {
  prometheus: {
    _class: "org.kairosdb.metrics4jplugin.prometheus.PrometheusSink"
    listen-port: 9090
  }
}

triggers: {
  prometheus: {
    _class: "org.kairosdb.metrics4jplugin.prometheus.PrometheusTrigger"
  }
}
```

* _listen-port:_ Port on which to listen for prometheus scrap requests

#### KairosSink

The Kairos sink is a separate jar that needs to be placed in the classpath along
with its dependencies.

```hocon
kairos: {
  _class: "org.kairosdb.metrics4jplugin.kairosdb.KairosSink"
  host-url: "http://192.168.1.55:8080"
  #telnet-host: "192.168.1.55"
  #telnet-port: 4242
}
```

Depending on which properties are set the sink will send either http or telnet using
the kairosdb client.

* _host-url:_ (**http://localhost**) Url endpoint for sending http metrics to kairosdb
* _telnet-host:_ (**null**) Telnet host to send metrics to kairosdb
* _telnet-port:_ (**4242**) Telnet port
* _ttl:_ (**0s**) Optional ttl.  Can be specified like so "60s" or "24h"

#### TimescaleDBSink

TODO: https://docs.timescale.com/latest/using-timescaledb/writing-data

#### StatsDTCPSink

Sends metrics to a StatsD instance.  You can also set the source property _statsd_type_ to specify
the type of metric, it defaults to 'g'

* _host:_ Host to connect to
* _port:_ Port to use
* _protocol:_ (UDP/**TCP**) Protocol to use
* _max-udp-packet-size:_ (**1024**) Max packet size when using UDP

### Collectors
A collector defines how to collect values from a source.  For reportSize() I could
use a LongCounter or a LongGauge.  When looking for a collector for a source metrics4j
will match the type so if you define both a LongCounter and a DoubleCounter it will 
know to grab the LongCounter as it inherits from LongCollector.  The following
collectors are all in the `org.kairosdb.metrics4j.collectors.impl` package.

#### DoubleCounter
Counts up double values to be reported.  The counter can be reset when values are reported
by setting reset: true in the conf

* _reset:_ (true/false), when true the counter resets after reporting
* _report-zero:_ (true/false), when set to false will not report zero values

#### LongCounter
Counts up long values to be reported.  The counter can be reset when values are reported
by setting reset: true in the conf

* _reset:_ (true/false), when true the counter resets after reporting
* _report-zero:_ (true/false), when set to false will not report zero values

#### LongGauge
Simple gauge that reports the most recently received value.

* _reset:_ (true/false), when true the gauge sets to zero after reporting

#### MaxLongGauge
Extends LongGauge and only stores and reports the max value over the reporting period.

* _reset:_ (true/false), when true the gauge sets to zero after reporting

#### DoubleGauge
Simple gauge that reports the most recently received value.

* _reset:_ (true/false), when true the gauge sets to zero after reporting

#### SimpleStats
This reports the min, max, sum, count and avg for the set of values received since
last reporting.

* _report-zero:_ (true/false), when set to false will not report zero values

#### SimpleTimerMetric
Used for reporting measured durations.  The collector reports min, max, total,
count and avg for the measurements received during the last reporting period.
Values are reported as milliseconds by default but maybe changed using the
report-unit attribute.

* _report-unit:_ (NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, DAYS), set 
the unites values are reported in
* _report-zero:_ (true/false), when set to false will not report zero values

#### StringReporter
No aggregation is done in this collector.  All strings are reported with the time
they were received.

### Formatters
A formatter can change the name to your liking ie. underscore vs period
in the name.

#### TemplateFormatter
class = org.kairosdb.metrics4j.formatters.TemplateFormatter

Pass a template attribute where you can placeholders for className, methodName,
field and specific tags.

```hocon
formatters: {
  templateWithStatus: {
    _class: "org.kairosdb.metrics4j.formatters.TemplateFormatter"
    template: "metric4j.%{className}.%{methodName}.%{tag.status}.%{field}"
  }
}
```

You can also use metricName which can be specified within the source.  This gives
you the option to explicitly name a metrics
```hocon
formatters.CustomFormatter: {
  _class: "org.kairosdb.metrics4j.formatters.TemplateFormatter"
  template: "Karios.Prefix.%{metricName}"
}
sources.com.kairosdb.CrazyClassNamePath: {
  _metric-name: "Better.Metric.Name"
  _formatter: "CustomerFormatter"
}
```

**Note** The template formatter uses `%{}` so as to not be confused with `${}` used
by hocon substitution - and so you can use both in a template. like so 
`template: ${metric-prefix}".%{className}.%{methodName}.%{tag.status}.%{field}"`  
I only quote the template replace portion.

* _template:_ template to use when formatting metric names, see above.

### Triggers
The trigger tells metrics4j when to gather the metrics from the collectors and 
report to the sinks.

#### IntervalTrigger
The IntervalTrigger lets you set a time for how often metrics are reported.  The
following reports metrics every 5 seconds.  The interval property is a hocon duration. 
https://github.com/lightbend/config/blob/master/HOCON.md#duration-format
```hocon
triggers: {
  myTrigger: {
    _class: "org.kairosdb.metrics4j.triggers.IntervalTrigger" 
    interval="5s"
  }
}
```

* _interval:_ Set the trigger interval to gather metrics.


### Plugins

#### JMXReporter
JMXReporter started as an external project that used metrics4j to report JMX metrics. https://github.com/kairosdb/JMXReporter
This code was brought inside Metrics4j so JMX metrics can be reported by simply 
including the JMXReporter plugin.
```hocon
metrics4j {
  plugins {
    jmx: {
      _class: "org.kairosdb.metrics4j.plugins.JMXReporter"
    }
  }
}
```

Once you have included the JMXReporter plugin add a _dump-file config and run your application.
When you shutdown your application all possible JMX metrics will be dumped to the config
file so you can turn on and off metrics you are looking for.