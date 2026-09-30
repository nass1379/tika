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

public class MediaType_image_2_0_Test {

    @Test
    public void testImageMethod() {
        // Test with a valid image type
        MediaType imageType = MediaType.image("png");
        assertNotNull(imageType);
        assertEquals("image/png", imageType.toString());
        // Test with an empty string
        MediaType emptyType = MediaType.image("");
        assertNotNull(emptyType);
        assertEquals("image/", emptyType.toString());
        // Test with a null string
        MediaType nullType = MediaType.image(null);
        assertNotNull(nullType);
        assertEquals("image/", nullType.toString());
    }
}
