package utils;

/**
 * Created by 周思成 on  2020/3/10 15:43
 */



/**
 * @author jiachun.fjc
 */
public final class AsciiStringUtil {

    public static byte[] unsafeEncode(final CharSequence in) {
        final int len = in.length();
        final byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            out[i] = (byte) in.charAt(i);
        }
        return out;
    }

//    public static String unsafeDecode(final byte[] in) {
//        final int len = in.length;
//        final char[] out = new char[len];
//        for (int i = 0; i < len; i++) {
//            out[i] = (char) (in[i] & 0xFF);
//        }
//        return UnsafeUtil.moveToString(out);
//    }

//    public static String unsafeDecode(final ByteString in) {
//        final int len = in.size();
//        final char[] out = new char[len];
//        for (int i = 0; i < len; i++) {
//            out[i] = (char) (in.byteAt(i) & 0xFF);
//        }
//        return UnsafeUtil.moveToString(out);
//    }

    private AsciiStringUtil() {
    }
}