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

public class EndianUtils_getIntLE_21_0_Test {

    @Test
    public void testGetIntLE() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 0;
        int expected = 0x04030201;
        int result = EndianUtils.getIntLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIntLEWithOffset() {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
        int offset = 4;
        int expected = 0x08070605;
        int result = EndianUtils.getIntLE(data, offset);
        assertEquals(expected, result);
    }

    @Test
    public void testGetIntLEWithEmptyArray() {
        byte[] data = {};
        int offset = 0;
        Executable executable = () -> EndianUtils.getIntLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }

    @Test
    public void testGetIntLEWithInsufficientData() {
        byte[] data = { 0x01, 0x02 };
        int offset = 0;
        Executable executable = () -> EndianUtils.getIntLE(data, offset);
        assertThrows(IndexOutOfBoundsException.class, executable);
    }
}
