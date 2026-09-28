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

public class EndianUtils_getUIntBE_26_0_Test {

    @Test
    public void testGetUIntBE() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = EndianUtils.getUIntBE(data);
        assertEquals(0L, result);
    }

    @Test
    public void testGetUIntBEWithOffset() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        long result = EndianUtils.getUIntBE(data, 4);
        assertEquals(1L, result);
    }

    @Test
    public void testGetUIntBEWithNegativeOffset() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUIntBE(data, -1));
    }

    @Test
    public void testGetUIntBEWithOffsetExceedingArrayLength() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getUIntBE(data, 8));
    }
}
