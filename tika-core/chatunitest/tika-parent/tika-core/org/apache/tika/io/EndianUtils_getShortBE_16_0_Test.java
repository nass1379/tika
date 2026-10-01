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
import org.mockito.*;
import org.junit.jupiter.api.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;

public class EndianUtils_getShortBE_16_0_Test {

    @Test
    public void testGetShortBE() {
        byte[] data = { 0x00, 0x01 };
        short result = EndianUtils.getShortBE(data);
        assertEquals(1, result);
    }

    @Test
    public void testGetShortBEWithOffset() {
        byte[] data = { 0x01, 0x00, 0x02, 0x03 };
        short result = EndianUtils.getShortBE(data, 1);
        assertEquals(0x0002, result);
    }

    @Test
    public void testGetShortBEWithEmptyArray() {
        byte[] data = {};
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getShortBE(data));
    }

    @Test
    public void testGetShortBEWithOffsetAndEmptyArray() {
        byte[] data = {};
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getShortBE(data, 1));
    }

    @Test
    public void testGetShortBEWithOffsetAndShortArray() {
        byte[] data = { 0x01 };
        assertThrows(IndexOutOfBoundsException.class, () -> EndianUtils.getShortBE(data, 1));
    }
}
