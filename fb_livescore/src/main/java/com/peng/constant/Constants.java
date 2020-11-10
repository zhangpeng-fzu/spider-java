package com.peng.constant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {

    //赛事
    public static final String[] MATCH_COLUMNS = new String[]{"赛事编号", "比赛时间", "赛事", "状态", "主队", "客队", "胜赔率", "平赔率", "负赔率", "比分", "赛果"};

    //串关分析
    public static final String[] MATCH_CASCADE_FIELD_ARR = new String[]{"ss", "sp", "sf", "ps", "pp", "pf", "fs", "fp", "ff"};
    public static final String[] MATCH_CASCADE_COLUMNS = new String[]{"胜胜", "胜平", "胜负", "平胜", "平平", "平负", "负胜", "负平", "负负"};
    public static final String[] MATCH_CASCADE_COLUMNS_DATE = new String[MATCH_CASCADE_COLUMNS.length + 1];

    //进球分析
    public static final String[] MATCH_NUM_COMMON = new String[]{"0球", "1球", "2球", "3球", "4球", "5球", "6球", "7球", "零球", "1球3球", "2球4球", "5球6球7球"};
    public static final String[] MATCH_NUM_OVERVIEW_COLUMNS = new String[MATCH_NUM_COMMON.length + 1];
    public static final String[] MATCH_NUM_DETAIL_COLUMNS = new String[MATCH_NUM_COMMON.length + 1];

    //半全场分析
    public static final String[] MATCH_HALF_COMMON = new String[]{"胜胜", "胜平", "胜负", "平胜", "平平", "平负", "负胜", "负平", "负负", "胜胜/负负", "胜负/负胜"};
    public static final String[] MATCH_HALF_OVERVIEW_COLUMNS = new String[MATCH_HALF_COMMON.length + 1];
    public static final String[] MATCH_HALF_DETAIL_COLUMNS = new String[MATCH_HALF_COMMON.length + 1];
    //统计数据
    public static final String TOTAL_MISS = "出现总次数";
    public static final String AVG_MISS = "平均遗漏值";
    public static final String MAX_MISS = "最大遗漏值";
    public static final String MAX_300_MISS = "300场最大遗漏值";
    public static final List<String> WEEK_DAYS = Arrays.asList("周日", "周一", "周二", "周三", "周四", "周五", "周六");
    public static final HashMap<String, String> MATCH_RES_MAP = new HashMap<String, String>() {
        private static final long serialVersionUID = 1520605194545292329L;

        {
            put("s", "胜");
            put("p", "平");
            put("f", "负");
        }
    };

    //用于进球对比的数据
    public static final String[][] INIT_COMPARE_DATA = {
            {"单", "单", "单", "单", "单", "单", "单", "单", "单", "单", "双", "双", "双", "双", "双", "双", "双", "双", "双", "双", "单", "双", "单", "单", "单", "单"},
            {"单", "单", "单", "单", "双", "双", "双", "双", "双", "双", "单", "单", "单", "单", "单", "单", "双", "双", "双", "双", "单", "双", "", "", "", ""},
            {"单", "双", "双", "双", "单", "单", "单", "双", "双", "双", "单", "单", "单", "双", "双", "双", "单", "单", "单", "双", "单", "双", "", "", "", ""},
            {"双", "单", "双", "双", "单", "双", "双", "单", "单", "双", "单", "双", "双", "单", "单", "双", "单", "单", "双", "单", "单", "双", "", "", "", ""},
            {"双", "双", "单", "双", "双", "单", "双", "单", "双", "单", "双", "单", "双", "单", "双", "单", "单", "双", "单", "单", "单", "双", "", "", "", ""},
            {"双", "双", "双", "单", "双", "双", "单", "双", "单", "单", "双", "双", "单", "双", "单", "单", "双", "单", "单", "单", "单", "双", "", "", "", ""},

    };

    //表格名称
    public static final String NUM_TABLE = "num";
    public static final String NUM_DETAIL_TABLE = "num_detail";
    public static final String CASCADE_TABLE = "cascade";
    public static final String CASCADE_DETAIL_TABLE = "cascade_detail";
    public static final String COMPARE_TABLE = "compare";
    public static final String COMPARE_DETAIL_TABLE = "compare_detail";
    public static final String HALF_TABLE = "half";
    public static final String HALF_DETAIL_TABLE = "half_detail";
    //进球对比
    public static String[] MATCH_COMPARE_OVERVIEW_COLUMNS;
    public static String[] MATCH_COMPARE_DETAIL_COLUMNS;
    public static String SELECT_MATCH_NUM = null;

    //记录最大值，用于80%判断
    public static Map<String, Map<String, String[]>> MAX_MISS_VALUE_MAP = new HashMap<>();
    public static Map<String, Map<String, String[]>> MAX_300_MISS_VALUE_MAP = new HashMap<>();
    public static Map<String, String[][]> TABLE_NAME_MAP = new HashMap<>();

    //进行数据初始化
    static {

        MATCH_NUM_OVERVIEW_COLUMNS[0] = "赛事编号";
        System.arraycopy(MATCH_NUM_COMMON, 0, MATCH_NUM_OVERVIEW_COLUMNS, 1, MATCH_NUM_COMMON.length);

        MATCH_NUM_DETAIL_COLUMNS[0] = "日期";
        System.arraycopy(MATCH_NUM_COMMON, 0, MATCH_NUM_DETAIL_COLUMNS, 1, MATCH_NUM_COMMON.length);

        MATCH_HALF_OVERVIEW_COLUMNS[0] = "赛事编号";
        System.arraycopy(MATCH_HALF_COMMON, 0, MATCH_HALF_OVERVIEW_COLUMNS, 1, MATCH_HALF_COMMON.length);

        MATCH_HALF_DETAIL_COLUMNS[0] = "日期";
        System.arraycopy(MATCH_HALF_COMMON, 0, MATCH_HALF_DETAIL_COLUMNS, 1, MATCH_HALF_COMMON.length);

        MATCH_CASCADE_COLUMNS_DATE[0] = "日期";
        System.arraycopy(MATCH_CASCADE_COLUMNS, 0, MATCH_CASCADE_COLUMNS_DATE, 1, MATCH_CASCADE_COLUMNS.length);

        MATCH_COMPARE_OVERVIEW_COLUMNS = new String[INIT_COMPARE_DATA[0].length * 2 + 1];
        MATCH_COMPARE_DETAIL_COLUMNS = new String[INIT_COMPARE_DATA[0].length * 2 + 4];

        MATCH_COMPARE_OVERVIEW_COLUMNS[0] = "赛事编号";
        MATCH_COMPARE_DETAIL_COLUMNS[0] = "日期";
        MATCH_COMPARE_DETAIL_COLUMNS[1] = "比分";
        MATCH_COMPARE_DETAIL_COLUMNS[2] = "进球数";
        MATCH_COMPARE_DETAIL_COLUMNS[3] = "单双爆";

        for (int i = 0; i < INIT_COMPARE_DATA[0].length; i++) {
            MATCH_COMPARE_OVERVIEW_COLUMNS[1 + 2 * i] = (i + 1) + "路";
            MATCH_COMPARE_OVERVIEW_COLUMNS[2 + 2 * i] = "对比";

            MATCH_COMPARE_DETAIL_COLUMNS[4 + 2 * i] = (i + 1) + "路";
            MATCH_COMPARE_DETAIL_COLUMNS[5 + 2 * i] = "对比";
        }

        TABLE_NAME_MAP.put(NUM_TABLE, new String[][]{MATCH_NUM_OVERVIEW_COLUMNS, MATCH_NUM_DETAIL_COLUMNS});
        TABLE_NAME_MAP.put(COMPARE_TABLE, new String[][]{MATCH_COMPARE_OVERVIEW_COLUMNS, MATCH_COMPARE_DETAIL_COLUMNS});
        TABLE_NAME_MAP.put(HALF_TABLE, new String[][]{MATCH_HALF_OVERVIEW_COLUMNS, MATCH_HALF_DETAIL_COLUMNS});

    }
}
