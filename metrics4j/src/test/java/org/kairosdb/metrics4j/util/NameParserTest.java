package org.kairosdb.metrics4j.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NameParserTest
{
    @Test
    public void testParseClassName()
    {
        assertThat(NameParser.parseSimpleClassName("com.kairos.MyClass")).isEqualTo("MyClass");
        assertThat(NameParser.parseSimpleClassName(".MyClass")).isEqualTo("MyClass");
        assertThat(NameParser.parseSimpleClassName("MyClass")).isEqualTo("MyClass");
        assertThat(NameParser.parseSimpleClassName("MyClass.")).isEqualTo("");
    }
}
