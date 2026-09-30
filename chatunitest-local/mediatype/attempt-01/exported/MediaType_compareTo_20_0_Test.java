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

public class MediaType_compareTo_20_0_Test {

    @Test
    public void testCompareTo() {
        MediaType type1 = new MediaType("application", "json");
        MediaType type2 = new MediaType("application", "xml");
        MediaType type3 = new MediaType("application", "json");
        // Same type and subtype
        assertEquals(0, type1.compareTo(type3));
        // Different subtype
        assertEquals(-1, type1.compareTo(type2));
        // Different subtype
        assertEquals(1, type2.compareTo(type1));
    }
}
