package com.peng.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {

    public static final String[] MATCH_NUM_FIELD_ARR = new String[]{"zero", "one", "two", "three", "four", "five", "six", "seven", "zero", "oneThree", "twoFour", "five_"};
    public static final String[] MATCH_CASCADE_FIELD_ARR = new String[]{"ss", "sp", "sf", "ps", "pp", "pf", "fs", "fp", "ff"};
    public static final String[] MATCH_COLUMNS = new String[]{"赛事编号", "比赛时间", "赛事", "状态", "主队", "客队", "胜赔率", "平赔率", "负赔率", "比分", "赛果"};
    public static final String[] MATCH_CASCADE_COLUMNS = new String[]{"胜胜", "胜平", "胜负", "平胜", "平平", "平负", "负胜", "负平", "负负"};
    public static final String[] MATCH_CASCADE_COLUMNS_DATE = new String[]{"日期", "胜胜", "胜平", "胜负", "平胜", "平平", "平负", "负胜", "负平", "负负"};
    public static final String[] MATCH_NUM_COLUMNS = new String[]{"赛事编号", "0球", "1球", "2球", "3球", "4球", "5球", "6球", "7球", "零球", "1球3球", "2球4球", "5球6球7球"};
    public static final String[] MATCH_NUM_COLUMNS_DATE = new String[]{"日期", "0球", "1球", "2球", "3球", "4球", "5球", "6球", "7球", "零球", "1球3球", "2球4球", "5球6球7球"};
    public static final String[] MATCH_COMPARE_COLUMNS = new String[]{"赛事编号", "1路", "对比", "2路", "对比", "3路", "对比", "4路", "对比", "5路", "对比", "6路", "对比", "7路", "对比", "8路", "对比", "9路", "对比", "10路", "对比"};
    public static final String[] MATCH_COMPARE_COLUMNS_DATE = new String[]{"日期", "比分", "进球数", "单双爆", "1路", "对比", "2路", "对比", "3路", "对比", "4路", "对比", "5路", "对比", "6路", "对比", "7路", "对比", "8路", "对比", "9路", "对比", "10路", "对比",};

    public static final String TOTAL_MISS = "出现总次数";
    public static final String AVG_MISS = "平均遗漏值";
    public static final String MAX_MISS = "最大遗漏值";
    // 基本数词表
    public static final String[] EN_NUM = {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
    public static final List<String> WEEK_DAYS = Arrays.asList("周日", "周一", "周二", "周三", "周四", "周五", "周六");


    public static final HashMap<String, String> MATCH_RES_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 1520605194545292329L;

        {
            put("s", "胜");
            put("p", "平");
            put("f", "负");
        }
    };

    public static final String FINISHED = "1";
    public static final String PLAYING = "0";
    public static final String CANCELLED = "2";
    public static final Map<String, String> MATCH_STATUS_MAP = new HashMap<>();

    public static final String[][] INIT_COMPARE_DATA = {
            {"双", "双", "双", "双", "双", "单", "单", "单", "单", "单"},
            {"双", "双", "单", "单", "双", "双", "双", "单", "单", "单"},
            {"单", "双", "双", "双", "单", "双", "双", "单", "双", "双"},
            {"双", "双", "单", "单", "单", "双", "双", "双", "双", "双"},
            {"双", "单", "双", "单", "单", "单", "单", "双", "双", "单"},
            {"单", "单", "单", "双", "单", "单", "单", "单", "单", "双"},
            {"单", "单", "双", "双", "双", "单", "双", "双", "单", "双"},
            {"双", "单", "单", "单", "双", "双", "双", "双", "单", "单"},
            {"单", "双", "单", "单", "双", "单", "单", "双", "双", "双"},
            {"单", "单", "双", "双", "单", "双", "单", "单", "双", "单"}
    };


    public static final String NUM_TABLE = "num";
    public static final String NUM_DETAIL_TABLE = "num_detail";
    public static final String CASCADE_TABLE = "cascade";
    public static final String CASCADE_DETAIL_TABLE = "cascade_detail";
    public static final String COMPARE_TABLE = "compare";
    public static final String COMPARE_DETAIL_TABLE = "compare_detail";
}
