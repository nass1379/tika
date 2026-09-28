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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests added outside ChatUniTest to distinguish the remaining PIT mutants. */
class EndianUtilsManualTest {
    private static ByteArrayInputStream bytes(int... values) {
        byte[] data = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            data[i] = (byte) values[i];
        }
        return new ByteArrayInputStream(data);
    }

    @Test
    void testReadShortBEValues() throws Exception {
        assertEquals(0x1234, EndianUtils.readShortBE(bytes(0x12, 0x34)));
        assertEquals(-2, EndianUtils.readShortBE(bytes(0xFF, 0xFE)));
    }

    @Test
    void testReadUnsignedShortBEValues() throws Exception {
        assertEquals(0x1234, EndianUtils.readUShortBE(bytes(0x12, 0x34)));
        assertEquals(65534, EndianUtils.readUShortBE(bytes(0xFF, 0xFE)));
    }

    @Test
    void testReadIntLEValues() throws Exception {
        assertEquals(0x78563412, EndianUtils.readIntLE(bytes(0x12, 0x34, 0x56, 0x78)));
        assertEquals(-2, EndianUtils.readIntLE(bytes(0xFE, 0xFF, 0xFF, 0xFF)));
    }

    @Test
    void testReadIntBEValues() throws Exception {
        assertEquals(0x12345678, EndianUtils.readIntBE(bytes(0x12, 0x34, 0x56, 0x78)));
        assertEquals(-2, EndianUtils.readIntBE(bytes(0xFF, 0xFF, 0xFF, 0xFE)));
    }

    @Test
    void testReadLongLEValues() throws Exception {
        assertEquals(0x0807060504030201L,
                EndianUtils.readLongLE(bytes(1, 2, 3, 4, 5, 6, 7, 8)));
        assertEquals(-2L, EndianUtils.readLongLE(bytes(0xFE, 0xFF, 0xFF, 0xFF,
                0xFF, 0xFF, 0xFF, 0xFF)));
    }

    @Test
    void testReadLongBEValues() throws Exception {
        assertEquals(0x0102030405060708L,
                EndianUtils.readLongBE(bytes(1, 2, 3, 4, 5, 6, 7, 8)));
        assertEquals(-2L, EndianUtils.readLongBE(bytes(0xFF, 0xFF, 0xFF, 0xFF,
                0xFF, 0xFF, 0xFF, 0xFE)));
    }

    @FunctionalInterface
    private interface Reader {
        long read(InputStream input) throws Exception;
    }

    private record FixedWidthReader(String name, int width, Reader reader) {
    }

    private static FixedWidthReader[] fixedWidthReaders() {
        return new FixedWidthReader[]{
                new FixedWidthReader("readShortLE", 2, EndianUtils::readShortLE),
                new FixedWidthReader("readShortBE", 2, EndianUtils::readShortBE),
                new FixedWidthReader("readUShortLE", 2, EndianUtils::readUShortLE),
                new FixedWidthReader("readUShortBE", 2, EndianUtils::readUShortBE),
                new FixedWidthReader("readUIntLE", 4, EndianUtils::readUIntLE),
                new FixedWidthReader("readUIntBE", 4, EndianUtils::readUIntBE),
                new FixedWidthReader("readIntLE", 4, EndianUtils::readIntLE),
                new FixedWidthReader("readIntBE", 4, EndianUtils::readIntBE),
                new FixedWidthReader("readIntME", 4, EndianUtils::readIntME),
                new FixedWidthReader("readLongLE", 8, EndianUtils::readLongLE),
                new FixedWidthReader("readLongBE", 8, EndianUtils::readLongBE)};
    }

    @Test
    void testFixedWidthZeroIsValid() throws Exception {
        for (FixedWidthReader spec : fixedWidthReaders()) {
            assertEquals(0L, spec.reader().read(new ByteArrayInputStream(new byte[spec.width()])),
                    spec.name());
        }
    }

    @Test
    void testFixedWidthTruncationAtEveryPosition() {
        for (FixedWidthReader spec : fixedWidthReaders()) {
            for (int length = 0; length < spec.width(); length++) {
                byte[] prefix = new byte[length];
                Arrays.fill(prefix, (byte) 0x12);
                assertThrows(EndianUtils.BufferUnderrunException.class,
                        () -> spec.reader().read(new ByteArrayInputStream(prefix)),
                        spec.name() + " with " + length + " bytes");
            }
        }
    }

    @Test
    void testIntBEArrayWithoutOffset() {
        assertEquals(0x12345678, EndianUtils.getIntBE(new byte[]{0x12, 0x34, 0x56, 0x78}));
    }

    @Test
    void testUnsignedArrayWrappersReturnNonZero() {
        assertEquals(0xFEDCBA98L, EndianUtils.getUIntBE(
                new byte[]{(byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98}));
        assertEquals(0xFEDCBA98L, EndianUtils.getUIntLE(
                new byte[]{(byte) 0x98, (byte) 0xBA, (byte) 0xDC, (byte) 0xFE}));
    }

    @Test
    void testGetUByteAtOffset() {
        byte[] data = {0x55, (byte) 0x80, (byte) 0xFF, 0};
        assertEquals(128, EndianUtils.getUByte(data, 1));
        assertEquals(255, EndianUtils.getUByte(data, 2));
        assertEquals(0, EndianUtils.getUByte(data, 3));
    }

    @Test
    void testReadUE7ZeroTerminalPreservesNextByte() throws Exception {
        ByteArrayInputStream input = bytes(0, 0x55);
        assertEquals(0L, EndianUtils.readUE7(input));
        assertEquals(0x55, input.read());
    }

    @Test
    void testReadUE7RejectsPrematureEnd() {
        assertThrows(IOException.class, () -> EndianUtils.readUE7(bytes()));
        assertThrows(IOException.class, () -> EndianUtils.readUE7(bytes(0x81)));
    }

    @Test
    void testReadUE7SixBytePayload() throws Exception {
        ByteArrayInputStream input = bytes(0x81, 0x82, 0x83, 0x84, 0x85, 0x06, 0x55);
        assertEquals(34902966918L, EndianUtils.readUE7(input));
        assertEquals(0x55, input.read());
    }

    @Test
    void testReadUE7SixByteLimitConsumesLookahead() throws Exception {
        // Characterizes the existing six-byte limit and its seventh-byte lookahead.
        // This is not a claim that silently truncating an overlong encoding is desirable.
        ByteArrayInputStream input = bytes(0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x07, 0x55);
        assertEquals(34902966918L, EndianUtils.readUE7(input));
        assertEquals(0x55, input.read());
    }
    @Test
    void testReadUE7ZeroAfterContinuation() throws Exception {
        ByteArrayInputStream input = bytes(0x81, 0, 0x55);
        assertEquals(128L, EndianUtils.readUE7(input));
        assertEquals(0x55, input.read());
    }

    @Test
    void testFixedWidthRejectsEofEvenIfFileGrows(@TempDir Path directory) throws Exception {
        for (FixedWidthReader spec : fixedWidthReaders()) {
            for (int eofPosition = 0; eofPosition < spec.width(); eofPosition++) {
                Path file = Files.createTempFile(directory, "endian-", ".bin");
                byte[] prefix = new byte[eofPosition];
                Arrays.fill(prefix, (byte) 0x12);
                Files.write(file, prefix);
                byte[] appended = new byte[spec.width() - eofPosition - 1];
                Arrays.fill(appended, (byte) 0x34);
                try (InputStream input = new FilterInputStream(new FileInputStream(file.toFile())) {
                    private boolean hasAppended;

                    @Override
                    public int read() throws IOException {
                        int value = super.read();
                        if (value == -1 && !hasAppended) {
                            // Deterministic file growth after the delegate observes EOF.
                            Files.write(file, appended, StandardOpenOption.APPEND);
                            hasAppended = true;
                        }
                        return value;
                    }
                }) {
                    assertThrows(EndianUtils.BufferUnderrunException.class,
                            () -> spec.reader().read(input),
                            spec.name() + " with EOF at position " + eofPosition);
                }
            }
        }
    }

}
