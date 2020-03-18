package utils;

/**
 * Created by 周思成 on  2020/3/13 22:58
 */

public class Ints {

    /**
     * Fast method of finding the next power of 2 greater than or equal to the supplied value.
     *
     * If the value is {@code <= 0} then 1 will be returned.
     * This method is not suitable for {@link Integer#MIN_VALUE} or numbers greater than 2^30.
     *
     * @param value from which to search for next power of 2
     * @return The next power of 2 or the value itself if it is a power of 2
     */
    public static int findNextPositivePowerOfTwo(final int value) {
        return value <= 0 ? 1 : value >= 0x40000000 ? 0x40000000 : 1 << (32 - Integer.numberOfLeadingZeros(value - 1));
    }
}
