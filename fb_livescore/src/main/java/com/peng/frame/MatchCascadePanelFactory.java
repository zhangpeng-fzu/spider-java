package com.peng.frame;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchCascadePanelFactory extends PaneFactory {
    private static MatchCascadePanelFactory matchCascadePanelFactory;

    static {
        matchCascadePanelFactory = new MatchCascadePanelFactory();
    }

    static MatchCascadePanelFactory getInstance() {
        return matchCascadePanelFactory;
    }

    /**
     * 按照日期获取串关数据
     *
     * @param date 选择日期
     * @return
     */
    JScrollPane showMatchCascadePaneByDate(Date date) {
        String[] columnNames = new String[]{"串关场次", "胜平负组合", "当前遗漏值", "赔率"};// 定义表格列名数组
        java.util.List<MatchCascadeBean> matchCascadeBeans = MatchCascadeRepository.getMatchCascadeData(date);
        String[][] rowData = new String[matchCascadeBeans.size() * 9][4];
        int column = 0;
        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            String[] matchNums = matchCascadeBean.getMatchCascadeNum().split("串");
            //只显示有比赛的场次
            if (!Constants.MATCH_STATUS_MAP.containsKey(matchNums[0]) || !Constants.MATCH_STATUS_MAP.containsKey(matchNums[1]) ||
                    (DateUtil.isToday(date) && (!isPlaying(Constants.MATCH_STATUS_MAP.get(matchNums[0])) || !isPlaying(Constants.MATCH_STATUS_MAP.get(matchNums[1]))))
                    || (!DateUtil.isToday(date) && (isUnFinished(Constants.MATCH_STATUS_MAP.get(matchNums[0])) || isUnFinished(Constants.MATCH_STATUS_MAP.get(matchNums[1])))
            )) {
                continue;
            }

            String[] odds = new String[9];
            if (matchCascadeBean.getOdds() != null && matchCascadeBean.getOdds().length() > 0) {
                odds = matchCascadeBean.getOdds().replace("[", "").replace("]", "").split(",");
            }

            for (int i = 0; i < odds.length; i++) {
                String odd = odds[i];
                if (odd != null && odd.trim().length() > 5) {
                    odd = odd.trim().substring(0, 4);
                }
                odds[i] = odd;
            }

            int j = column * 9;
            String[] columns = Constants.MATCH_CASCADE_COLUMNS;
            for (int i = 0; i < Constants.MATCH_CASCADE_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchCascadeBean.class.getDeclaredField(Constants.MATCH_CASCADE_FIELD_ARR[i]);
                    field.setAccessible(true);
                    rowData[j + i] = new String[]{matchCascadeBean.getMatchCascadeNum(), columns[i], String.valueOf(field.get(matchCascadeBean)), odds[i]};

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            column++;
        }
        String[][] newRowData = new String[column * 9][4];
        if (column * 9 >= 0) {
            System.arraycopy(rowData, 0, newRowData, 0, column * 9);
        }


        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{2, 3});
        return new JScrollPane(table);
    }

    /**
     * 获取串关详细数据
     *
     * @param matchCascadeNum 串关编号
     * @return
     */
    JScrollPane showMatchCascadePaneByNum(String matchCascadeNum) {
        java.util.List<MatchCascadeBean> matchCascadeBeans = MatchCascadeRepository.getMatchCascadeDataByNum(matchCascadeNum);
        String[][] rowData = new String[matchCascadeBeans.size() + 3][10];
        String[] matchNums = matchCascadeNum.split("串");

        //获取前面编号的状态
        List<MatchBean> matchBeans = LiveDataRepository.getMatchListByNum(matchNums[0]);
        Map<String, String> matchStatusMapByNum1 = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum1.put(matchBean.getLiveDate(), matchBean.getStatus());
        }

        //获取后面编号的状态
        matchBeans = LiveDataRepository.getMatchListByNum(matchNums[1]);
        Map<String, String> matchStatusMapByNum2 = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum2.put(matchBean.getLiveDate(), matchBean.getStatus());
        }
        int column = 0;
        int[] matchCascadeCountArr = new int[9];
        int[] matchCascadeMaxArr = new int[9];
        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            //不显示已取消或者不存在的场次
            String date = DateUtil.getDateFormat().format(matchCascadeBean.getLiveDate());
            if (!matchStatusMapByNum1.containsKey(date) || !matchStatusMapByNum2.containsKey(date) ||
                    isCancelled(matchStatusMapByNum1.get(date)) || isCancelled(matchStatusMapByNum2.get(date))) {
                continue;
            }
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    (isUnFinished(matchStatusMapByNum1.get(date)) || isUnFinished(matchStatusMapByNum2.get(date)))) {
                rowData[column] = new String[10];
                rowData[column][0] = DateUtil.getDateFormat(1).format(matchCascadeBean.getLiveDate());
                column++;
                continue;
            }
            rowData[column] = new String[10];
            rowData[column][0] = DateUtil.getDateFormat(1).format(matchCascadeBean.getLiveDate());

            for (int i = 0; i < Constants.MATCH_CASCADE_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchCascadeBean.class.getDeclaredField(Constants.MATCH_CASCADE_FIELD_ARR[i]);
                    field.setAccessible(true);
                    int value = Integer.parseInt(String.valueOf(field.get(matchCascadeBean)));
                    if (value == 0) {
                        matchCascadeCountArr[i]++;
                    }
                    matchCascadeMaxArr[i] = Math.max(matchCascadeMaxArr[i], value);
                    rowData[column][i + 1] = String.valueOf(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            column++;
        }
        //增加统计数据
        addStatisticsData(column, 10, rowData, matchCascadeCountArr, matchCascadeMaxArr);
        column = column + 3;
        String[][] newRowData = new String[column][10];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, Constants.MATCH_CASCADE_COLUMNS_DATE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        return setPanelScroll(table);
    }

}
