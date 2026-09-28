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

public class EndianUtils_getShortBE_17_0_Test {

    @Test
    public void testGetShortBE() {
        byte[] data = { 0x00, 0x01 };
        int offset = 0;
        short expected = 0x0100;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithOffset() {
        byte[] data = { 0x01, 0x00, 0x02, 0x03 };
        int offset = 1;
        short expected = 0x0002;
        short result = EndianUtils.getShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetShortBEWithNegativeOffset() {
        byte[] data = { 0x00, 0x01 };
        int offset = -1;
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> EndianUtils.getShortBE(data, offset));
    }

    @Test
    public void testGetShortBEWithOffsetGreaterThanArrayLength() {
        byte[] data = { 0x00, 0x01 };
        int offset = 2;
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> EndianUtils.getShortBE(data, offset));
    }
}
