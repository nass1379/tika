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

public class EndianUtils_getUShortBE_18_0_Test {

    @Test
    public void testGetUShortBE() {
        byte[] data = { 0x00, 0x01 };
        int result = EndianUtils.getUShortBE(data);
        assertEquals(0x0001, result);
    }

    @Test
    public void testGetUShortBEWithOffset() {
        byte[] data = { 0x00, 0x01, 0x02, 0x03 };
        int result = EndianUtils.getUShortBE(data, 1);
        assertEquals(0x0102, result);
    }

    @Test
    public void testGetUShortBEWithEmptyArray() {
        byte[] data = {};
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortBE(data));
    }

    @Test
    public void testGetUShortBEWithSingleElementArray() {
        byte[] data = { 0x00 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUShortBE(data));
    }
}
