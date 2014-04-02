package co.kuznetsov.util;

import java.nio.ByteBuffer;


public class ByteBufferUtils {
    private final static byte[] LONG_MIN_VALUE_BYTES = "-9223372036854775808".getBytes();
    private final static byte[] INT_MIN_VALUE_BYTES = "-2147483648".getBytes();

    private final static int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};

    public static void writeIntAsString(ByteBuffer bb, int value) {
        if (value == Integer.MIN_VALUE) {
            bb.put(INT_MIN_VALUE_BYTES);
            return;
        }
        int size = (value < 0) ? stringSizeInt(-value) + 1 : stringSizeInt(value);
        writeIntDigitsDirectly(bb, value, size);
    }

    public static void writeLongAsString(ByteBuffer bb, long value) {
        if (value == Long.MIN_VALUE) {
            bb.put(LONG_MIN_VALUE_BYTES);
            return;
        }
        int size = (value < 0) ? stringSizeLong(-value) + 1 : stringSizeLong(value);
        writeLongDigitsDirectly(bb, value, size);
    }

    // Requires positive x
    private static int stringSizeInt(int x) {
        for (int i = 0; ; i++)
            if (x <= sizeTable[i])
                return i + 1;
    }

    // Requires positive x
    private static int stringSizeLong(long x) {
        long p = 10;
        for (int i = 1; i < 19; i++) {
            if (x < p)
                return i;
            p = 10 * p;
        }
        return 19;
    }

    private static void writeIntDigitsDirectly(ByteBuffer bb, int i, int size) {
        int q, r;
        int charPos = bb.position() + size - 1;
        byte sign = 0;

        bb.position(charPos + 1);

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            bb.put(charPos--, DigitOnes[r]);
            bb.put(charPos--, DigitTens[r]);
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (; ; ) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            bb.put(charPos--, digits[r]);
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            bb.put(charPos, sign);
        }
    }

    private static void writeLongDigitsDirectly(ByteBuffer bb, long i, int size) {
        long q;
        int r;
        int charPos =  bb.position() + size - 1;
        byte sign = 0;

        bb.position(charPos + 1);

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Get 2 digits/iteration using longs until quotient fits into an int
        while (i > Integer.MAX_VALUE) {
            q = i / 100;
            // really: r = i - (q * 100);
            r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
            i = q;
            bb.put(charPos--, DigitOnes[r]);
            bb.put(charPos--, DigitTens[r]);
        }

        // Get 2 digits/iteration using ints
        int q2;
        int i2 = (int) i;
        while (i2 >= 65536) {
            q2 = i2 / 100;
            // really: r = i2 - (q * 100);
            r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
            i2 = q2;
            bb.put(charPos--, DigitOnes[r]);
            bb.put(charPos--, DigitTens[r]);
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i2 <= 65536, i2);
        for (; ; ) {
            q2 = (i2 * 52429) >>> (16 + 3);
            r = i2 - ((q2 << 3) + (q2 << 1));  // r = i2-(q2*10) ...
            bb.put(charPos--, digits[r]);
            i2 = q2;
            if (i2 == 0) break;
        }
        if (sign != 0) {
            bb.put(charPos, sign);
        }
    }


    final static byte[] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    };

    final static byte[] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    };

    final static byte[] digits = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z'
    };

}
