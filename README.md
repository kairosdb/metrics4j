# metrics4j
Library for abstracting the reporting of metrics in your code from sending them to a time series service.

This library is still in development, keep checking back as progress is moving quickly

## Philosophy of using Metrics4j
The metrics4j library is designed to separate the role of the application developer
from the IT administrator that deploys the software.  Metrics4j has only one dependency 
Slf4j for logging.  The lack of dependencies is important if this library is to
be used in various projects.  The lack of dependencies is also why metrics4j uses
XML for configuration.  Java's built in support for XML is really good and does
a lot of work for the project without requiring other libraries.

### Application Developer
The application developer's role is to identify interesting metrics and report them.
The library lets you report numbers or durations (for timing something).  All values you
report are sent to a collector.  The interpretation of those numbers, ie. is it a guage
or rate, is determined at runtime by the administrator.

### IT Administrator
The IT Admin is the one that deploys the application and is able, by configuration at runtime,
to determine the following
1. What metrics to send
1. How often to send
1. Where to send the metrics
1. Name of the metric and format
1. How to interpret the metric, is it a guage, rate or counter?

All of the above is determined through configuration via the metrics4j.xml file.  In some
cases additional jar files may need to be placed in the classpath.

The rest of the documentation is split into 2 sections, the first is for developers
using the library and the second section is for admins trying to configure the library
in a deployed application.

# Section 1 (Developer)

## Using the library
Anyone wanting to instrument their code will only have to do three things.
1.  Create an interface that describes the metric to report.
1.  Use the MetricSourceManager to instantate an instance of the above interface.
1.  Call the method to report metrics.

Actual reporting of the metrics and where they are reporting to will be determined 
at run time based on a configuration file. (covered in section 2)

Each metric reported consists of a value (long, double or string) a timestamp (determined
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

The return type for the method indicates what type of value will be collected.
Our example collects long values and durations.

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
name so it is a good idea to name them something appropriate.

## Different ways to report metrics

Put values into a collector

Duration helper methods

Custom MetricCollector


## Testing with the library
As any good developer will do you will want to test your code to make sure
it reports metrics.
The following example is using mockito but any mock library will work.

```java
//mock the counter and register it with ReporterFactory for a specific call
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

When Metrics4j loads it will try to find a file named metrics4j.xml in the classpath.
If metrics4j.xml is not found all the reporting methods are effectively no-ops.
 
## Configuration
The top level of the configuration file looks like this
```xml
<metrics4j>
	<sources name="ROOT"> <!-- Defines sources and associates sinks, collectors, formatters and triggers with them -->
		...
	</sources>
	
	<sinks> <!-- Defines what sinks to use - where to send the metrics -->
		...
	</sinks>
	
	<collectors> <!-- Defines what collectors will be used and configures them -->
		...
	</collectors>
	
	<formatters> <!-- Can reformat metric names and tags when reporting -->
		...
	</formatters>

	<triggers> <!-- determines when to collect metrics -->
		...
	</triggers>
</metrics4j>
```

Collectors are cloned for each source, Sinks, Formatters and Triggers are treated
as singletons.

JAXB is used when reading each object so configuration can be passed.  For example
when using the TemplateFormatter you can specify a template attribute that is the 
template.

In some cases you may have a plugin that has conflicting dependencies with 
the main application that Metrics4j is being used in.  In this case you can 
use the plugin feature of Metrics4j by specifying a folder attribute on the 
component you are including.  The folder attribute defines a location to find jar
files that contain the component (collector, sink, etc).  These jars are loaded
in a separate class loader and isolated to prevent conflicts.

### Sources
The purpose of sources is to associate a sink/collector/formatter/trigger with 
the various sources of metrics throughout the application.

Lets look at the previous example of MessageSizeReporter and I want to set reportSize() 
to use a counter, this is how that would look (assuming MessageSizeReporter was in package foo.com):

```xml
<metrics4j>
	<sources>
		<source name="com.foo.MessageSizeReporter.reportSize">
			<collector ref="Counter"/>
		</source>
	</sources>
	<collectors>
		<collector name="Counter" class="org.kairosdb.metrics4j.collectors.LongCounter" reset="true"/>
	</collectors>
	...
</metrics4j>
```

When MessageSizeReporter is created metrics4j will search up the tree looking for a
defined LongCollector for reportSize to return.  If I knew I wanted all collectors
to be the same then I could reference the collector at the root once and all would
use it.  The above collector is configured to reset its value after it is reported.

The source name is the path to help metrics4j find the configuration it needs.  The name
attribute is a dot delimited path or you can specify one path component per source like this:

```xml
<metrics4j>
	<sources>
		<source name="com">
			<source name="foo">
				<source name="MessageSizeReporter.reportSize">
					<collector ref="Counter"/>
				</source>
			</source>
		</source
	<sources>
	...
</metrics4j>
```

Defaults can be specified at the root of sources and then overridden at any level
that is convenient for you.

For each source (reportSize()) you can define a collector, a trigger, a formatter and 
zero or more sinks.


### Sinks
A sink defines a destination to send the metrics to.

#### Slf4JMetricSink
Reports metrics to an Slf4j logger.  The logLevel attribute controls the log level.

#### TelnetSink


### Collectors
A collector defines how to collect values from a source.  For reportSize() I could
use a LongCounter or a LongGauge.  When looking for a collector for a source metrics4j
will match the type so if you define both a LongCounter and a DoubleCounter it will 
know to grab the LongCounter as it inherits from LongCollector.

#### DoubleCounter


#### LongCounter
Counts up values to be reported.  The counter can be reset when values are reported
by setting reset="true" in the xml

#### LongGauge
Simple gauge that reports the most recently received value.

#### SimpleStats
This reports the min, max, sum, count and avg for the set of values received since
last reporting.

#### SimpleTimerMetric


#### StringReporter

### Formatters
A formatter can change just about anything on a metric before it is reported.  
The primary purpose is to format the name to your liking ie. underscore vs period
in the name.  A formatter can also change the tags, timestamp and value.

#### TemplateFormatter
class = org.kairosdb.metrics4j.formatters.TemplateFormatter

Pass a template attribute where you can placeholders for className, methodName,
field and specific tags.

```xml
<formatter name="templateWithStatus" class="org.kairosdb.metrics4j.formatters.TemplateFormatter"
  template="metric4j.${className}.${methodName}.${tag.status}.${field}"/>
```

### Triggers
The trigger tells metrics4j when to gather the metrics from the collectors and 
report to the sinks.

#### IntervalTrigger
The IntervalTrigger lets you set a time for how often metrics are reported.  The
following reports metrics every 5 seconds.  Units can be anything as specified in the
java.util.concurrent.TimeUnit class.
```xml
<trigger name="myTrigger" class="org.kairosdb.metrics4j.triggers.IntervalTrigger" interval="5" unit="SECONDS"/>
```


