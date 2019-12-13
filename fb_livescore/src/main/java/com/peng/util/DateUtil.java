package com.peng.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dateFormatForCascade = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dateFormatForNum = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat dateFormat_cn = new SimpleDateFormat("yyyy年MM月dd日");

    public static SimpleDateFormat getDateFormat() {
        return getDateFormat(0);
    }

    public static SimpleDateFormat getDateFormat(int type) {
        switch (type) {
            case 1:
                return dateFormat_cn;
            case 2:
                return dateFormatForCascade;
            case 3:
                return dateFormatForNum;
            default:
                return dateFormat;
        }
    }

    public static boolean isToday(Date date) {
        return DateUtil.getDateFormat().format(date).equals(DateUtil.getDateFormat().format(new Date()));
    }

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
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }
}
