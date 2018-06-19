/**
 * Copyright (c) Microsoft Corporation
 * <p/>
 * All rights reserved.
 * <p/>
 */
package com.microsoft.correlationvector;

import java.nio.ByteBuffer;

/**
 * This class provides method to encode byte buffers into base64 strings. This
 * is for backwards compatibility for java 7 and earlier.
 */
public class Base64Encoder {

    private static final String BASE64_TABLE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    /**
     * Encodes the given byte buffer into base64 string.
     *
     * @param bytes
     *            byte buffer.
     * @return base64 string.
     */
    public static String toBase64String(ByteBuffer bytes) {
        final int len = bytes.capacity();
        final int outputLength = (int) Math.ceil((len * 8) / 6.0);
        final char[] sb = new char[outputLength];
        final byte[] buffer = new byte[3];

        for (int i = 0, j = 0; i < len;) {
            buffer[i % 3] = bytes.get(i);
            ++i;
            if ((i % 3) == 0) {
                sb[j] = BASE64_TABLE.charAt((buffer[0] & 0xFC) >> 2);
                sb[j + 1] = BASE64_TABLE.charAt(((buffer[0] & 0x03) << 4) + ((buffer[1] & 0xF0) >> 4));
                sb[j + 2] = BASE64_TABLE.charAt(((buffer[1] & 0x0F) << 2) + ((buffer[2] & 0xC0) >> 6));
                sb[j + 3] = BASE64_TABLE.charAt(buffer[2] & 0x3F);
                j += 4;
            }
        }

        final int remainingBytes = len % 3;
        if (remainingBytes > 0) {
            for (int i = remainingBytes; i < 3; ++i) {
                buffer[i] = '\0';
            }

            // Remaining Bytes can only be 1 or 2
            if (remainingBytes == 1) {
                sb[outputLength - 2] = BASE64_TABLE.charAt((buffer[0] & 0xFC) >> 2);
                sb[outputLength - 1] = BASE64_TABLE.charAt((buffer[0] & 0x03) << 4);
            } else {
                sb[outputLength - 3] = BASE64_TABLE.charAt((buffer[0] & 0xFC) >> 2);
                sb[outputLength - 2] = BASE64_TABLE.charAt(((buffer[0] & 0x03) << 4) + ((buffer[1] & 0xF0) >> 4));
                sb[outputLength - 1] = BASE64_TABLE.charAt((buffer[1] & 0x0F) << 2);
            }
        }

        return new String(sb);
    }
}
