package com.peng.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static Date getBjTime() throws IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8")); // 时区设置
        URL url = new URL("http://www.baidu.com");//取得资源对象
        URLConnection uc = url.openConnection();//生成连接对象
        uc.connect(); //发出连接
        long ld = uc.getDate(); //取得网站日期时间（时间戳）
        if (ld == 0) {
            return new Date();
        }
        return new Date(ld);
    }

    /**
     * 计算日期偏移量
     *
     * @param c 比赛数据中的WeekDay
     * @param w 比赛数据中日期实际对应的WeekDay
     * @return
     */
    public static int calculateDateOffset(int c, int w) {
        int offset = c - w;
        if (c == 6 && w == 0) {
            offset = -1;
        }
        if (c == 0 && w == 6) {
            offset = 1;
        }
        if (c == 6 && w == 1) {
            offset = -2;
        }
        if (c == 1 && w == 6) {
            offset = 2;
        }
        if (c == 0 && w == 2) {
            offset = -2;
        }
        if (c == 2 && w == 0) {
            offset = 2;
        }
        if (c == 5 && w == 0) {
            offset = -2;
        }
        if (c == 0 && w == 5) {
            offset = 2;
        }
        return offset;
    }


    public static Date getTomorrow() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar.getTime();
    }
}
