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

public class MediaType_equals_18_0_Test {

    @Test
    public void testEqualsWithSameObject() {
        MediaType mediaType = MediaType.OCTET_STREAM;
        assertTrue(mediaType.equals(mediaType));
    }

    @Test
    public void testEqualsWithDifferentObject() {
        MediaType mediaType1 = MediaType.OCTET_STREAM;
        MediaType mediaType2 = MediaType.TEXT_PLAIN;
        assertFalse(mediaType1.equals(mediaType2));
    }

    @Test
    public void testEqualsWithNull() {
        MediaType mediaType = MediaType.OCTET_STREAM;
        assertFalse(mediaType.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        MediaType mediaType = MediaType.OCTET_STREAM;
        assertFalse(mediaType.equals("application/octet-stream"));
    }
}
