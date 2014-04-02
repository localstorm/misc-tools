package co.kuznetsov.util;

public class StringBuilderUtils {

    public static boolean startsWith(StringBuilder sb, String s) {
        if (sb.length() < s.length()) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != sb.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public static boolean endsWith(StringBuilder sb, String s) {
        int len = sb.length();
        if (len < s.length()) {
            return false;
        }
        int offset = len - s.length();
        for (int i = 0; i < s.length(); i++) {
            if (sb.charAt(offset + i) != s.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public static boolean equalsTo(StringBuilder sb, String s) {
        if (sb.length() != s.length()) {
            return false;
        }

        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != sb.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public static int parseInt(StringBuilder sb, int offset, int radix) throws NumberFormatException {
        if (sb == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = offset, len = sb.length() - offset;
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = sb.charAt(offset);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    nfe(sb.substring(offset));

                if (len == 1) // Cannot have lone "+" or "-"
                    nfe(sb.substring(offset));
                i++;
            }
            multmin = limit / radix;
            while (i - offset < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(sb.charAt(i++),radix);
                if (digit < 0) {
                    nfe(sb.substring(offset));
                }
                if (result < multmin) {
                    nfe(sb.substring(offset));
                }
                result *= radix;
                if (result < limit + digit) {
                    nfe(sb.substring(offset));
                }
                result -= digit;
            }
        } else {
            nfe(sb.substring(offset));
        }
        return negative ? result : -result;
    }

    public static long parseLong(StringBuilder sb, int offset, int radix) throws NumberFormatException {
        if (sb == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " less than Character.MIN_RADIX");
        }
        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                    " greater than Character.MAX_RADIX");
        }

        long result = 0;
        boolean negative = false;
        int i = offset, len = sb.length() - offset;
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = sb.charAt(offset);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    nfe(sb.substring(offset));

                if (len == 1) // Cannot have lone "+" or "-"
                    nfe(sb.substring(offset));
                i++;
            }
            multmin = limit / radix;
            while (i - offset < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(sb.charAt(i++), radix);
                if (digit < 0) {
                    nfe(sb.substring(offset));
                }
                if (result < multmin) {
                    nfe(sb.substring(offset));
                }
                result *= radix;
                if (result < limit + digit) {
                    nfe(sb.substring(offset));
                }
                result -= digit;
            }
        } else {
            nfe(sb.substring(offset));
        }
        return negative ? result : -result;
    }

    public static int writeBytes(byte[] dest, StringBuilder value) {
        int arrayPos = 0;
        for (int i=0; i < value.length() && arrayPos < dest.length; i++) {
            char c = value.charAt(i);
            if (c <= 255) {
                dest[arrayPos++] = (byte) c;
            } else {
                dest[arrayPos++] = (byte) (c >>> 8) ;
                dest[arrayPos++] = (byte) (c & 255) ;
            }
        }
        return arrayPos;
    }


    private static int nfe(String s) {
        throw new NumberFormatException("Illegal integer: " + s);
    }
}
