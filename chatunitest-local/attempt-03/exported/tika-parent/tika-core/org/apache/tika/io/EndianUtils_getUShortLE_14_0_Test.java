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

public class EndianUtils_getUShortLE_14_0_Test {

    @Test
    public void testGetUShortLE() {
        byte[] data = { 0x01, 0x02 };
        int result = EndianUtils.getUShortLE(data);
        assertEquals(0x0201, result);
    }

    @Test
    public void testGetUShortLEWithOffset() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04 };
        int result = EndianUtils.getUShortLE(data, 1);
        assertEquals(0x0403, result);
    }

    @Test
    public void testGetUShortLEWithEmptyArray() {
        byte[] data = {};
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortLE(data));
    }

    @Test
    public void testGetUShortLEWithSingleElementArray() {
        byte[] data = { 0x01 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortLE(data));
    }
}
