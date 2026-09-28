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

public class EndianUtils_getUShortBE_19_0_Test {

    @Test
    public void testGetUShortBE() {
        byte[] data = { 0x01, 0x02 };
        int offset = 0;
        int expected = 0x0102;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithOffset() {
        byte[] data = { 0x03, 0x04, 0x05, 0x06 };
        int offset = 2;
        int expected = 0x0506;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithLeadingZero() {
        byte[] data = { 0x00, 0x01 };
        int offset = 0;
        int expected = 0x0001;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithTrailingZero() {
        byte[] data = { 0x01, 0x00 };
        int offset = 0;
        int expected = 0x0100;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUShortBEWithNegativeValues() {
        byte[] data = { (byte) 0xFF, (byte) 0xFE };
        int offset = 0;
        int expected = 0xFFFE;
        int result = EndianUtils.getUShortBE(data, offset);
        assertEquals(expected, result);
    }
}
