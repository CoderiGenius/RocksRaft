package utils;

/**
 * Created by 周思成 on  2020/3/13 16:45
 */

public interface Copiable<T> {

    /**
     * Copy current object(deep-clone).
     */
    T copy();
}
