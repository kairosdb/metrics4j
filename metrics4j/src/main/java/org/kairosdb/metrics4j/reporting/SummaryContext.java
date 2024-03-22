package org.kairosdb.metrics4j.reporting;

public class SummaryContext
{
	public enum TYPE
	{
		SUM,
		COUNT,
		QUANTILE
	}

	public static final SummaryContext SUM_CONTEXT = new SummaryContext(TYPE.SUM, 0.0);
	public static final SummaryContext COUNT_CONTEXT = new SummaryContext(TYPE.COUNT, 0.0);

	private final TYPE m_summaryType;
	private final double m_quantileValue;

	public static SummaryContext createQuantile(double quantile)
	{
		return new SummaryContext(TYPE.QUANTILE, quantile);
	}

	private SummaryContext(TYPE summaryType, double quantileValue)
	{
		m_summaryType = summaryType;
		m_quantileValue = quantileValue;
	}

	public TYPE getSummaryType()
	{
		return m_summaryType;
	}

	public double getQuantileValue()
	{
		return m_quantileValue;
	}
}
