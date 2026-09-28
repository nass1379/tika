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

public class EndianUtils_getUShortLE_15_0_Test {

    @Test
    public void testGetUShortLE() {
        byte[] data = { 0x01, 0x02 };
        int offset = 0;
        int expected = 0x0201;
        int result = EndianUtils.getUShortLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortLEWithOffset() {
        byte[] data = { 0x03, 0x04, 0x05, 0x06 };
        int offset = 2;
        int expected = 0x0605;
        int result = EndianUtils.getUShortLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortLEWithNegativeOffset() {
        byte[] data = { 0x07, 0x08 };
        int offset = -1;
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortLE(data, offset));
    }

    @Test
    public void testGetUShortLEWithOffsetPastArray() {
        byte[] data = { 0x09, 0x0A };
        int offset = 2;
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortLE(data, offset));
    }
}
