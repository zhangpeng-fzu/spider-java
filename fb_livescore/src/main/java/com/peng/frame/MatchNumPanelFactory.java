package com.peng.frame;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchNumBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchNumRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchNumPanelFactory extends PaneFactory {
    private static MatchNumPanelFactory matchNumPanelFactory;

    static {
        matchNumPanelFactory = new MatchNumPanelFactory();
    }

    Map<String, String> matchStatusMap = new HashMap<>();
    private JFrame innerFrame = null;

    static MatchNumPanelFactory getInstance() {
        return matchNumPanelFactory;
    }

    /**
     * 按照日期获取进球数据
     *
     * @param date 选择日期
     * @return
     */
    JScrollPane showMatchNumPaneByDate(Date date) {
        String[] columnNames = Constants.MATCH_NUM_COLUMNS;
        java.util.List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumData(date);
        String[][] rowData = new String[matchNumBeans.size()][16];
        int column = 0;
        for (MatchNumBean matchNumBean : matchNumBeans) {
            //只显示未完成的场次
            if (!matchStatusMap.containsKey(matchNumBean.getMatchNum()) ||
                    (DateUtil.isToday(date) && !isPlaying(matchStatusMap.get(matchNumBean.getMatchNum())))
                    || (!DateUtil.isToday(date) && isUnFinished(matchStatusMap.get(matchNumBean.getMatchNum())))) {
                continue;
            }
            rowData[column] = new String[16];
            rowData[column][0] = matchNumBean.getMatchNum();
            for (int i = 0; i < Constants.MATCH_NUM_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchNumBean.class.getDeclaredField(Constants.MATCH_NUM_FIELD_ARR[i]);
                    field.setAccessible(true);
                    rowData[column][i + 1] = String.valueOf(field.get(matchNumBean));

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            column++;
        }
        String[][] newRowData = new String[column][16];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
        return new JScrollPane(table);
    }

    /**
     * 进球数据详情页
     *
     * @param matchNum 比赛场次
     * @return
     */
    JScrollPane showMatchNumPaneByNum(String matchNum) {
        String[] columnNames = Constants.MATCH_NUM_COLUMNS_DATE;
        java.util.List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumDataByNum(matchNum);
        String[][] rowData = new String[matchNumBeans.size() + 3][16];
        int column = 0;

        List<MatchBean> matchBeans = LiveDataRepository.getMatchListByNum(matchNum);
        Map<String, MatchBean> matchStatusMapByNum = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum.put(matchBean.getLiveDate(), matchBean);
        }
        int[] matchNumCountArr = new int[15];
        int[] matchNumMaxArr = new int[15];

        for (MatchNumBean matchNumBean : matchNumBeans) {
            String date = DateUtil.getDateFormat().format(matchNumBean.getLiveDate());
            //不显示已取消或者不存在的场次
            if (!matchStatusMapByNum.containsKey(date) || isCancelled(matchStatusMapByNum.get(date).getStatus())) {
                continue;
            }
            //当天未完成的场次 显示空行
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    isUnFinished(matchStatusMapByNum.get(date).getStatus())) {
                rowData[column] = new String[16];
                rowData[column][0] = DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate());
                column++;
                continue;
            }
            String[] missValues = new String[15];
            for (int i = 0; i < Constants.MATCH_NUM_FIELD_ARR.length; i++) {
                String filedName = Constants.MATCH_NUM_FIELD_ARR[i];
                try {
                    Field field = MatchNumBean.class.getDeclaredField(filedName);
                    field.setAccessible(true);
                    missValues[i] = String.valueOf(field.get(matchNumBean));
                    if (isHit(String.valueOf(field.get(matchNumBean)))) {
                        missValues[i] = matchStatusMapByNum.get(date).getHostNum() + ":" + matchStatusMapByNum.get(date).getGuestNum();
                        matchNumCountArr[i]++;
                    }
                    matchNumMaxArr[i] = Math.max(matchNumMaxArr[i], Integer.parseInt(String.valueOf(field.get(matchNumBean))));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            rowData[column] = new String[16];
            rowData[column][0] = DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate());
            System.arraycopy(missValues, 0, rowData[column], 1, missValues.length);
            column++;
        }
        addStatisticsData(column, 16, rowData, matchNumCountArr, matchNumMaxArr);

        String[][] newRowData = new String[++column][16];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

        return setPanelScroll(table);
    }
}