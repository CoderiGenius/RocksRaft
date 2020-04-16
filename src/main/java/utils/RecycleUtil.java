package utils;

/**
 * Recycle tool for {@link Recyclable}.
 *
 * @author jiachun.fjc
 */
public final class RecycleUtil {

    /**
     * Recycle designated instance.
     */
    public static boolean recycle(final Object obj) {
        return obj instanceof Recyclable && ((Recyclable) obj).recycle();
    }

    private RecycleUtil() {
    }
}
