package com.peng.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class ArrayUtil {
    public static int[] string2IntArray(String value, int size) {
        if (StringUtils.isBlank(value)) {
            return new int[size];
        }
        String[] arr = value.substring(1, value.length() - 1).split(",");
        return Arrays.stream(arr).mapToInt(i -> Integer.valueOf(i.trim())).toArray();
    }
}
