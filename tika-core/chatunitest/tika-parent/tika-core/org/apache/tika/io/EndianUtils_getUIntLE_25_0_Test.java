/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

public class EndianUtils_getUIntLE_25_0_Test {

    @Test
    public void testGetUIntLE() {
        byte[] data = { 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
        int offset = 4;
        long expected = 0L;
        long result = EndianUtils.getUIntLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntLEWithNegativeValues() {
        byte[] data = { (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 0xFFFFFFFFL;
        long result = EndianUtils.getUIntLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetUIntLEWithZeroValues() {
        byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        int offset = 0;
        long expected = 0L;
        long result = EndianUtils.getUIntLE(data, offset);
        assertEquals(expected, result);
    }
}
