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

public class MediaType_audio_1_0_Test {

    @Test
    public void testAudioMethod() {
        // Test with a valid audio type
        MediaType audioType = MediaType.audio("mp3");
        assertEquals("audio/mp3", audioType.toString());
        // Test with a valid audio type with parameters
        MediaType audioTypeWithParams = MediaType.audio("mp3; codecs=\"mp3\"; sample-rate=\"44100\"");
        assertEquals("audio/mp3; codecs=mp3; sample-rate=44100", audioTypeWithParams.toString());
        // Test with an empty type
        MediaType emptyType = MediaType.audio("");
        assertNull(emptyType);
        // Test with a type containing special characters
        MediaType specialType = MediaType.audio("mp3; foo=\"bar baz\"");
        assertEquals("audio/mp3; foo=\"bar baz\"", specialType.toString());
    }
}
