package co.kuznetsov.util;

public class ByteArrayUtils {

    public static int getInt(byte[] data, int offset) {
        return (((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16) |
                ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff));
    }

    public static long getLong(byte[] data, int offset) {
        return (((long)data[offset] << 56) +
                ((long)(data[offset + 1] & 255) << 48) +
                ((long)(data[offset + 2] & 255) << 40) +
                ((long)(data[offset + 3] & 255) << 32) +
                ((long)(data[offset + 4] & 255) << 24) +
                ((data[offset + 5] & 255) << 16) +
                ((data[offset + 6] & 255) <<  8) +
                (data[offset + 7] & 255));
    }
}
