package org.apache.tika.mime;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ExtendWith(MockitoExtension.class)
public class MediaType_hasParameters_15_0_Test {

    @Test
    public void testHasParametersWithEmptyParameters() {
        MediaType mediaType = new MediaType("text", "plain", Collections.emptyMap());
        assertFalse(mediaType.hasParameters());
    }

    @Test
    public void testHasParametersWithNonEmptyParameters() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("charset", "UTF-8");
        MediaType mediaType = new MediaType("text", "plain", parameters);
        assertTrue(mediaType.hasParameters());
    }
}
