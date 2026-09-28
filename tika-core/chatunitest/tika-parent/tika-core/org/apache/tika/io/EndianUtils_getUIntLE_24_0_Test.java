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

public class EndianUtils_getUIntLE_24_0_Test {

    @Test
    public void testGetUIntLE() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = EndianUtils.getUIntLE(data);
        assertEquals(0L, result);
    }

    @Test
    public void testGetUIntLEWithOffset() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = EndianUtils.getUIntLE(data, 4);
        assertEquals(0x01000000L, result);
    }

    @Test
    public void testGetUIntLEWithNegativeOffset() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUIntLE(data, -1));
    }

    @Test
    public void testGetUIntLEWithOffsetExceedingArrayLength() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUIntLE(data, 8));
    }
}
