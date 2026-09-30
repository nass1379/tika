// Tests manuels élaborés avec l'aide de ChatGPT/Codex.
package org.apache.tika.mime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MediaTypeManualTest {

    @Test
    void charsetBeforeMediaTypeIsPreserved() {
        MediaType result = MediaType.parse("charset=UTF-8; text/plain");

        assertNotNull(result);
        assertEquals("text", result.getType());
        assertEquals("plain", result.getSubtype());
        assertEquals("UTF-8", result.getParameters().get("charset"));
    }
    
    @Test
    void setOfMediaTypesRemovesDuplicatesAndNulls() {
        java.util.Set<MediaType> result = MediaType.set(
                MediaType.TEXT_PLAIN, null,
                MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML);

        assertEquals(
                java.util.Set.of(MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML),
                result);
        assertThrows(UnsupportedOperationException.class,
                () -> result.add(MediaType.OCTET_STREAM));
    }

    @Test
    void setOfStringsParsesAndFiltersInvalidValues() {
        java.util.Set<MediaType> result = MediaType.set(
                "text/plain", null, "invalid",
                "text/plain", "application/xml");

        assertEquals(
                java.util.Set.of(MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML),
                result);
        assertThrows(UnsupportedOperationException.class,
                () -> result.add(MediaType.OCTET_STREAM));
    }
    
    @Test
    void addingParametersToBaseTypePreservesValues() {
        MediaType result = new MediaType(
                MediaType.TEXT_PLAIN, java.util.Map.of("charset", "UTF-8"));

        assertEquals("text/plain", result.getBaseType().toString());
        assertEquals(java.util.Map.of("charset", "UTF-8"),
                result.getParameters());
    }

    @Test
    void addingEmptyParametersPreservesExistingValues() {
        MediaType base = new MediaType(
                MediaType.TEXT_PLAIN, "charset", "UTF-8");
        MediaType result = new MediaType(base, java.util.Map.of());

        assertEquals(base, result);
        assertEquals(java.util.Map.of("charset", "UTF-8"),
                result.getParameters());
    }

    @Test
    void addingParametersMergesAndOverridesValues() {
        MediaType base = new MediaType(MediaType.TEXT_PLAIN,
                java.util.Map.of("charset", "UTF-8", "format", "flowed"));

        MediaType result = new MediaType(base,
                java.util.Map.of("charset", "ISO-8859-1", "version", "1.0"));

        assertEquals(java.util.Map.of(
                "charset", "ISO-8859-1",
                "format", "flowed",
                "version", "1.0"), result.getParameters());

        assertEquals(java.util.Map.of(
                "charset", "UTF-8",
                "format", "flowed"), base.getParameters());
    }

    @Test
    void videoFactoryCreatesExpectedMediaType() {
        MediaType result = MediaType.video("mp4");

        assertNotNull(result);
        assertEquals("video", result.getType());
        assertEquals("mp4", result.getSubtype());
        assertEquals("video/mp4", result.toString());
    }

    @Test
    void baseTypeWithoutParametersReturnsSameInstance() {
        MediaType type = new MediaType("text", "plain");

        assertSame(type, type.getBaseType());
    }
    
    @Test
    void newlyParsedSimpleTypeEndingInZIsCached() {
        String input = "application/x-ift3913-mediatype-cache-z";

        MediaType first = MediaType.parse(input);
        MediaType second = MediaType.parse(input);

        assertNotNull(first);
        assertEquals(input, first.toString());
        assertSame(first, second);
    }
}