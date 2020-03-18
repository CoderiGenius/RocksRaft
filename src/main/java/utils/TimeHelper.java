package utils;

/**
 * Created by 周思成 on  2020/3/12 13:49
 */
import org.joda.time.DateTime;
/**
 * 全局时间工具
 * 单例模式
 */
public class TimeHelper {

    private static DateTime dateTime = new DateTime();

    private TimeHelper(){}

    public static DateTime getDateTime(){
        return dateTime;
    }
}
