package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getIntBE_23_0_Test {

    @Test
    public void testGetIntBE() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int offset = 0;
        int expected = 0x01020304;
        int result = EndianUtils.getIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIntBEWithOffset() {
        byte[] data = { 0x00, 0x00, 0x01, 0x02, 0x03, 0x04 };
        int offset = 2;
        int expected = 0x01020304;
        int result = EndianUtils.getIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIntBEWithNegativeOffset() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int offset = -1;
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> EndianUtils.getIntBE(data, offset));
    }

    @Test
    public void testGetIntBEWithTooSmallArray() {
        byte[] data = { 0x01, 0x02 };
        int offset = 0;
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> EndianUtils.getIntBE(data, offset));
    }
}
