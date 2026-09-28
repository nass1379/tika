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

public class EndianUtils_ubyteToInt_29_0_Test {

    @Test
    public void testUbyteToInt() {
        // Test with a positive byte value
        byte positiveByte = 10;
        int result = EndianUtils.ubyteToInt(positiveByte);
        assertEquals(10, result);
        // Test with a negative byte value
        byte negativeByte = -10;
        result = EndianUtils.ubyteToInt(negativeByte);
        assertEquals(256 - 10, result);
        // Test with the maximum byte value
        byte maxByte = (byte) 0xFF;
        result = EndianUtils.ubyteToInt(maxByte);
        assertEquals(255, result);
        // Test with the minimum byte value
        byte minByte = (byte) 0x00;
        result = EndianUtils.ubyteToInt(minByte);
        assertEquals(0, result);
    }
}
