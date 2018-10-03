package org.kairosdb.metrics4j;

public class Main
{
	private static TestReporter reporter = ReporterFactory.createReporter(TestReporter.class);

	public static void main(String[] args)
	{
		System.out.print("System is up and running");

		reporter.reportSize("myTag").add(1);
	}
}
