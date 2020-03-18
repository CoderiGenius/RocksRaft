package entity;

/**
 * Created by 周思成 on  2020/3/10 15:30
 */

/**
 * Checksum for entity.
 *
 * @author boyan(boyan@antfin.com)
 * @since 1.2.6
 */
public interface Checksum {

    /**
     * Caculate a checksum value for this entity.
     * @return checksum value
     */
    long checksum();

    /**
     * Returns the checksum value of two long values.
     *
     * @param v1 first long value
     * @param v2 second long value
     * @return checksum value
     */
    default long checksum(final long v1, final long v2) {
        return v1 ^ v2;
    }
}