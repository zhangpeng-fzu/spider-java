package com.peng.util;

import java.text.SimpleDateFormat;

public class DateUtil {
    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    static SimpleDateFormat dateFormat_cn = new SimpleDateFormat("yyyy年MM月dd日");

    public static SimpleDateFormat getDateFormat() {
        return getDateFormat(0);
    }

    public static SimpleDateFormat getDateFormat(int type) {
        if (type == 0) {
            return dateFormat;
        } else {
            return dateFormat_cn;
        }
    }
}
