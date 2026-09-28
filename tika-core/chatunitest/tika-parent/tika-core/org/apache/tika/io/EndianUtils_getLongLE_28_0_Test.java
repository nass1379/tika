package org.apache.tika.io;

import org.junit.jupiter.api.function.Executable;
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getLongLE_28_0_Test {

    @Test
    public void testGetLongLE() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 0;
        long expected = 0x0807060504030201L;
        long result = EndianUtils.getLongLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetLongLEWithNegativeOffset() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = -1;
        Executable executable = () -> EndianUtils.getLongLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetLongLEWithOffsetExceedingArrayLength() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 8;
        Executable executable = () -> EndianUtils.getLongLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetLongLEWithOffsetExceedingArrayLengthByOne() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 9;
        Executable executable = () -> EndianUtils.getLongLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }
}
