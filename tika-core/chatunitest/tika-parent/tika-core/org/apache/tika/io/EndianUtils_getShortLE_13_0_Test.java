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

public class EndianUtils_getShortLE_13_0_Test {

    @Test
    public void testGetShortLE() {
        byte[] data = { 0x01, 0x02 };
        int offset = 0;
        short expected = 0x0201;
        short result = EndianUtils.getShortLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithOffset() {
        byte[] data = { 0x03, 0x04, 0x05, 0x06 };
        int offset = 2;
        short expected = 0x0605;
        short result = EndianUtils.getShortLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithEmptyArray() {
        byte[] data = {};
        int offset = 0;
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> EndianUtils.getShortLE(data, offset));
    }

    @Test
    public void testGetShortLEWithOffsetOutOfBounds() {
        byte[] data = { 0x07, 0x08 };
        int offset = 2;
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> EndianUtils.getShortLE(data, offset));
    }
}
