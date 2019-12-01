package com.peng.util;

import java.text.SimpleDateFormat;
import java.util.Date;

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
}
