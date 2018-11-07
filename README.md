# metrics4j
Library for abstracting the reporting of metrics in your code from sending them to a time series service.

This library is still in development, keep checking back as progress is moving quickly

## Using the library
Anyone wanting to instrument their code will only have to do three things.
1.  Create an interface that describes the metric to report.
1.  Use the ReporterFactory to instantate an instance of the above interface.
1.  Call the method to report metrics.

Actual reporting of the metrics and where they are reporting to will be determined 
at run time based on a configuration file. 

Lets look at an example of how this would be done.  Lets say you have a service
that receives messages and you want to report the amount of data your service is
receiving.  First step is to create an interface in your code that defines what 
you are reporting:
```java
public interface MessageSizeReporter
   {
   	Counter reportSize(@Key("host") String host);
   }
```

Notice the `@Key("host")` annotation on the host parameter.  This lets metrics4j 
know that you want to specify the host tag each time this is called.

You can have more than one method on your interface for example you may want to 
report the time it took to process the message.

Now in the class where you want to report your metric you will get an instance
of `MessageSizeReporter` from the ReporterFactory static method

```java
public static MessageSizeReporter reporter = ReporterFactory.getReporter(MessageSizeReporter.class);
```

Then in your code you will call the `reportSize` method and pass the size
of the message on to the Counter that is returned.

```java
reporter.reportSize(host).add(messageSize);
```

That's it.  You may be wondering what your metric name will look like?  Well 
that isn't up to you, it's up to whomever configures and runs your software.
Both the interface name and the method name are available for formatting the metric
name so it is a good idea to name them something appropriate.

## Testing with the library
The following example is using mockito but any mock library will work.

```java
//mock the counter and register it with ReporterFactory for a specific call
Counter myCounter = mock(Counter.class);
ReporterFactory.setStatsForMetric(myCounter, MessageSizeReporter.class).reportSize("localhost");
```

First create a mock Counter object.  Then we register the mock with the ReporterFactory
for when `MessageSizeReporter.reportSize` method is called with the host equal to
'localhost'.  This lets us be very specific as to when metrics4j is to use our mock
object.  At the end of our test we can verify that our mock was called with specific 
parameters

```java
verify(myCounter).add(42);
```

## Configuration
Items to control through configuration:
1.  What metrics to report
1.  What plugin to use to report metrics (kairos, influx, prometheous, etc..)
1.  How often to report metrics - specific to plugin
1.  Additional tags
1.  What the metric name format looks like