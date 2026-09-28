package org.apache.tika.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.EndianUtils;
import org.apache.tika.io.EndianUtils.BufferUnderrunException;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

public class EndianUtils_readShortLE_0_0_Test {

    private InputStream mockInputStream;

    @BeforeEach
    public void setUp() {
        mockInputStream = mock(InputStream.class);
    }

    @Test
    public void testReadShortLE_withValidData() throws IOException, TikaException {
        when(mockInputStream.read()).thenReturn(0x12, 0x34);
        short result = EndianUtils.readShortLE(mockInputStream);
        assertEquals(0x3412, result);
    }

    @Test
    public void testReadShortLE_withBufferUnderrun() throws IOException, TikaException {
        when(mockInputStream.read()).thenReturn(0x12, -1);
        assertThrows(BufferUnderrunException.class, () -> EndianUtils.readShortLE(mockInputStream));
    }

    @Test
    public void testReadShortLE_withNegativeData() throws IOException, TikaException {
        when(mockInputStream.read()).thenReturn(-1, -1);
        assertThrows(BufferUnderrunException.class, () -> EndianUtils.readShortLE(mockInputStream));
    }

    @Test
    public void testReadShortLE_withIOException() throws IOException, TikaException {
        when(mockInputStream.read()).thenThrow(new IOException());
        assertThrows(IOException.class, () -> EndianUtils.readShortLE(mockInputStream));
    }
}
