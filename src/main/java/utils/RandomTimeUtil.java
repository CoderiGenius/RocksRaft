package utils;

/**
 * Created by 周思成 on  2020/3/24 14:36
 */

public class RandomTimeUtil {


    public static long newRandomTime(long begin,long end){
        long rtn = begin + (long)(Math.random() * (end - begin));
        // 如果返回的是开始时间和结束时间，则递归调用本函数查找随机值
        if(rtn == begin || rtn == end){
            return newRandomTime(begin,end);
        }
        return rtn;
    }
}
