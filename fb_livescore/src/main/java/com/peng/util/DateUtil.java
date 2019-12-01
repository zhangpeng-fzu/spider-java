package com.peng.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
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
}
