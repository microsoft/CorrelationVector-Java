package com.microsoft.correlationvector;

import java.nio.ByteBuffer;

public class Base64Encoder {

    private static final String base64Table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

    public static String toBase64String(ByteBuffer bytes)
    {
        int len = bytes.capacity();
        int outputLength = (int)Math.ceil(len * 8 / 6.0);
        char[] sb = new char[outputLength];
        byte[] buffer = new byte[3];

        for (int i = 0, j = 0; i < len;)
        {
            buffer[i % 3] = bytes.get(i);
            ++i;
            if (i % 3 == 0)
            {
                sb[j] = base64Table.charAt((buffer[0] & 0xFC) >> 2);
                sb[j + 1] = base64Table.charAt(((buffer[0] & 0x03) << 4) + ((buffer[1] & 0xF0) >> 4));
                sb[j + 2] = base64Table.charAt(((buffer[1] & 0x0F) << 2) + ((buffer[2] & 0xC0) >> 6));
                sb[j + 3] = base64Table.charAt(buffer[2] & 0x3F);
                j += 4;
            }
        }

        int remainingBytes = len % 3;
        if (remainingBytes > 0)
        {
            for (int i = remainingBytes; i < 3; ++i)
            {
                buffer[i] = '\0';
            }

            // Remaining Bytes can only be 1 or 2
            if (remainingBytes == 1)
            {
                sb[outputLength - 2] = base64Table.charAt((buffer[0] & 0xFC) >> 2);
                sb[outputLength - 1] = base64Table.charAt((buffer[0] & 0x03) << 4);
            }
            else
            {
                sb[outputLength - 3] = base64Table.charAt((buffer[0] & 0xFC) >> 2);
                sb[outputLength - 2] = base64Table.charAt(((buffer[0] & 0x03) << 4) + ((buffer[1] & 0xF0) >> 4));
                sb[outputLength - 1] = base64Table.charAt((buffer[1] & 0x0F) << 2);
            }
        }

        return new String(sb);
    }
}
