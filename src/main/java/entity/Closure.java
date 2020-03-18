package entity;

/**
 * Created by 周思成 on  2020/3/11 16:02
 */
/**
 * Callback closure.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-03 11:07:05 AM
 */
public interface Closure {

    /**
     * Called when task is done.
     *
     * @param status the task status.
     */
    void run(final Status status);
}
