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

public class EndianUtils_getShortLE_12_0_Test {

    @Test
    public void testGetShortLE() {
        byte[] data = { 0x12, 0x34 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithOffset() {
        byte[] data = { 0x00, 0x12, 0x34, 0x56 };
        short expected = 0x3412;
        short result = EndianUtils.getShortLE(data, 1);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortLEWithEmptyArray() {
        byte[] data = {};
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getShortLE(data));
    }

    @Test
    public void testGetShortLEWithOffsetAndEmptyArray() {
        byte[] data = {};
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getShortLE(data, 1));
    }

    @Test
    public void testGetShortLEWithNegativeOffset() {
        byte[] data = { 0x12, 0x34 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getShortLE(data, -1));
    }

    @Test
    public void testGetShortLEWithOffsetExceedingArrayLength() {
        byte[] data = { 0x12, 0x34 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getShortLE(data, 3));
    }
}
