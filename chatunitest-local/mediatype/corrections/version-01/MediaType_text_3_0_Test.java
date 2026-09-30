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

public class MediaType_text_3_0_Test {

    @Test
    public void testTextMethodWithValidType() {
        MediaType result = MediaType.text("plain");
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testTextMethodWithWhitespaceType() {
        MediaType result = MediaType.text(" plain ");
        assertEquals("text/plain", result.toString());
    }

    @Test
    public void testTextMethodWithSpecialCharactersType() {
        MediaType result = MediaType.text("plain; charset=UTF-8");
        assertEquals("text/plain; charset=UTF-8", result.toString());
    }
}
