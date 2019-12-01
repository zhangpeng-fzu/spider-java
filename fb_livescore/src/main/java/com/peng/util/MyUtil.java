package com.peng.util;

public class MyUtil {
    public static String formatMatchNum(int i) {
        if (i < 10) {
            return "00" + i;
        }
        if (i < 100) {
            return "0" + i;
        }
        return String.valueOf(i);
    }
}
