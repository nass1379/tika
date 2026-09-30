package org.apache.tika.mime;

import org.apache.tika.mime.MediaType;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MediaType_parse_7_0_Test {

    @Test
    public void testParse_NullInput() {
        assertNull(MediaType.parse(null));
    }

    @Test
    public void testParse_SimpleType() {
        MediaType mediaType = MediaType.parse("text/plain");
        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubtype());
        assertTrue(mediaType.getParameters().isEmpty());
    }

    @Test
    public void testParse_ComplexType() {
        MediaType mediaType = MediaType.parse("application/xml; charset=UTF-8");
        assertNotNull(mediaType);
        assertEquals("application", mediaType.getType());
        assertEquals("xml", mediaType.getSubtype());
        assertEquals("UTF-8", mediaType.getParameters().get("charset"));
    }

    @Test
    public void testParse_UnregisteredType() {
        MediaType mediaType = MediaType.parse("invalid/type");
        assertNotNull(mediaType);
        assertEquals("invalid", mediaType.getType());
        assertEquals("type", mediaType.getSubtype());
    }

    @Test
    public void testParse_UnknownCharsetPreserved() {
        MediaType mediaType = MediaType.parse("text/plain; charset=invalid");
        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubtype());
        assertEquals("invalid", mediaType.getParameters().get("charset"));
    }

    @Test
    public void testParse_Whitespace() {
        MediaType mediaType = MediaType.parse("  text/plain  ");
        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubtype());
        assertTrue(mediaType.getParameters().isEmpty());
    }

    @Test
    public void testParse_QuotedValue() {
        MediaType mediaType = MediaType.parse("application/xml; charset=\"UTF-8\"");
        assertNotNull(mediaType);
        assertEquals("application", mediaType.getType());
        assertEquals("xml", mediaType.getSubtype());
        assertEquals("UTF-8", mediaType.getParameters().get("charset"));
    }

    @Test
    public void testParse_EmptyParameters() {
        MediaType mediaType = MediaType.parse("text/plain;");
        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubtype());
        assertTrue(mediaType.getParameters().isEmpty());
    }

    @Test
    public void testParse_SemicolonAtEnd() {
        MediaType mediaType = MediaType.parse("text/plain;");
        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubtype());
        assertTrue(mediaType.getParameters().isEmpty());
    }

    @Test
    public void testParse_MultipleParameters() {
        MediaType mediaType = MediaType.parse("application/xml; charset=UTF-8; version=1.0");
        assertNotNull(mediaType);
        assertEquals("application", mediaType.getType());
        assertEquals("xml", mediaType.getSubtype());
        assertEquals("UTF-8", mediaType.getParameters().get("charset"));
        assertEquals("1.0", mediaType.getParameters().get("version"));
    }

    @Test
    public void testParse_SpecialCharacters() {
        MediaType mediaType = MediaType.parse("application/xml; charset=\"UTF-8\"; name=\"example\"");
        assertNotNull(mediaType);
        assertEquals("application", mediaType.getType());
        assertEquals("xml", mediaType.getSubtype());
        assertEquals("UTF-8", mediaType.getParameters().get("charset"));
        assertEquals("example", mediaType.getParameters().get("name"));
    }

    @Test
    public void testParse_SimpleCache() {
        MediaType mediaType1 = MediaType.parse("text/plain");
        MediaType mediaType2 = MediaType.parse("text/plain");
        assertSame(mediaType1, mediaType2);
    }

    @Test
    public void testParse_ComplexTypesEqual() {
        MediaType mediaType1 = MediaType.parse("application/xml; charset=UTF-8");
        MediaType mediaType2 = MediaType.parse("application/xml; charset=UTF-8");
        assertNotNull(mediaType1);
        assertEquals(mediaType1, mediaType2);
    }
    @Test
    public void testParse_SimpleCacheMiss() {
        MediaType mediaType1 = MediaType.parse("text/plain");
        MediaType mediaType2 = MediaType.parse("text/html");
        assertNotSame(mediaType1, mediaType2);
    }

    @Test
    public void testParse_ComplexCacheMiss() {
        MediaType mediaType1 = MediaType.parse("application/xml; charset=UTF-8");
        MediaType mediaType2 = MediaType.parse("application/xml; charset=ISO-8859-1");
        assertNotSame(mediaType1, mediaType2);
    }
}
