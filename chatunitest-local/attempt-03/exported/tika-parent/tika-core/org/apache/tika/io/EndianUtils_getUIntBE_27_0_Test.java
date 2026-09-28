package org.apache.tika.io;

import org.apache.tika.io.EndianUtils;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EndianUtils_getUIntBE_27_0_Test {

    @Test
    public void testGetUIntBE() {
        byte[] data = { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
        int offset = 4;
        long expected = 1L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntBEWithNegativeNumber() {
        byte[] data = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 0xFFFFFFFFL;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntBEWithZero() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 0L;
        long result = EndianUtils.getUIntBE(data, offset);
        assertEquals(expected, result);
    }
}
