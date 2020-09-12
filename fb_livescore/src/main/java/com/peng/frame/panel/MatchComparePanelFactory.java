package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchNumBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchNumRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MatchComparePanelFactory extends PaneFactory {
    private static MatchComparePanelFactory matchNumPanelFactory;

    static {
        matchNumPanelFactory = new MatchComparePanelFactory();
    }

    public static MatchComparePanelFactory getInstance() {
        return matchNumPanelFactory;
    }


    /**
     * 按照日期获取进球数据
     *
     * @param date 选择日期
     * @return
     */
    public JScrollPane showMatchComparePaneByDate(Date date) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS;
        int size = columnNames.length;
        List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumData(date);
        String[][] rowData = new String[Math.max(matchNumBeans.size(), 10)][size];
        int column = 0;
        for (MatchNumBean matchNumBean : matchNumBeans) {
            //只显示未完成的场次
            if (MatchNumPanelFactory.skipMatchNum(date, matchNumBean.getMatchNum())) {
                continue;
            }
            rowData[column][0] = matchNumBean.getMatchNum();


            String[][] missValueData = this.getMissValueData(matchNumBean.getMatchNum());

            //使用今天的预设数据和昨天的遗漏数据拼出概览数据
            String[] todayMiss = missValueData[missValueData.length - 1];
            String[] yesterdayMiss = missValueData[missValueData.length - 2];
            for (int i = 0; i < todayMiss.length; i++) {
                if (i % 2 != 0) {
                    todayMiss[i] = yesterdayMiss[i];
                }
            }
            System.arraycopy(todayMiss, 0, rowData[column], 1, todayMiss.length);
            column++;
        }

        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.COMPARE_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
        return new JScrollPane(table);
    }

    private String[][] getMissValueData(String matchNum) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS_DATE;
        int size = columnNames.length - 4;
        List<MatchBean> matchList = LiveDataRepository.getMatchListByNum(matchNum);
        String[][] rowData = new String[matchList.size()][size];
        int column = 0;
        String[] lastMissValues = new String[size];
        Arrays.fill(lastMissValues, "0");
        String today = DateUtil.getDateFormat().format(new Date());
        for (int index = 0; index < matchList.size(); index++) {
            MatchBean matchBean = matchList.get(index);
            String[] curCompareData = Constants.INIT_COMPARE_DATA[index % 10];

            //以往，未完成或者已取消的场次
            if (!matchBean.getLiveDate().equals(today) && isUnFinished(matchBean.getStatus())) {
                column++;
                continue;
            }

            //当天的场次 显示预设数据
            if (matchBean.getLiveDate().equals(today)) {
                rowData[column] = new String[size];
                for (int i = 0; i < curCompareData.length; i++) {
                    rowData[column][i * 2] = curCompareData[i];
                }
                column++;
                continue;
            }
            String[] missValues = calcMissValue(matchBean, curCompareData, lastMissValues, null, null);
            lastMissValues = missValues;

            rowData[column] = new String[size];
            rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
            System.arraycopy(missValues, 0, rowData[column], 0, missValues.length);
            column++;
        }
        return rowData;
    }

    private String[] calcMissValue(MatchBean matchBean, String[] curCompareData, String[] lastMissValues, int[] matchCompareCountArr, int[] matchCompareMaxArr) {

        String[] missValues = new String[lastMissValues.length];
        String matchStatus = matchBean.getMatchStatus();
        for (int i = 0; i < Constants.INIT_COMPARE_DATA.length; i++) {
            if (curCompareData[i].equals("null")) {
                missValues[i] = "";
                continue;
            }
            missValues[2 * i] = curCompareData[i];
            if (matchStatus.equals(curCompareData[i])) {
                missValues[2 * i + 1] = "中";
                if (matchCompareCountArr != null) {
                    matchCompareCountArr[i]++;
                }
            } else {
                missValues[2 * i + 1] = String.valueOf(Integer.parseInt(lastMissValues[i * 2 + 1].equals("中") ? "0" : lastMissValues[i * 2 + 1]) + 1);
                if (matchCompareMaxArr != null) {
                    matchCompareMaxArr[i] = Math.max(matchCompareMaxArr[i], Integer.parseInt(missValues[2 * i + 1]));
                }
            }
        }
        return missValues;
    }

    /**
     * 进球数据详情页
     *
     * @param matchNum 比赛场次
     * @return
     */
    JScrollPane showMatchComparePaneByNum(String matchNum) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS_DATE;
        int size = columnNames.length;
        List<MatchBean> matchList = LiveDataRepository.getMatchListByNum(matchNum);
        String[][] rowData = new String[matchList.size() + 3][size];
        int column = 0;
        String[] lastMissValues = new String[20];
        Arrays.fill(lastMissValues, "0");
        int[] matchCompareCountArr = new int[10];
        int[] matchCompareMaxArr = new int[10];

        String today = DateUtil.getDateFormat().format(new Date());
        for (int index = 0; index < matchList.size(); index++) {
            MatchBean matchBean = matchList.get(index);
            String[] curCompareData = Constants.INIT_COMPARE_DATA[index % 10];

            //以往，未完成或者已取消的场次
            if (!matchBean.getLiveDate().equals(today) && isUnFinished(matchBean.getStatus())) {
                continue;
            }

            //当天未完成的场次 显示空行
            if (matchBean.getLiveDate().equals(today)) {
                rowData[column] = new String[size];
                rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
                for (int i = 0; i < curCompareData.length; i++) {
                    rowData[column][4 + i * 2] = curCompareData[i];
                }
                column++;
                continue;
            }
            String[] missValues = calcMissValue(matchBean, curCompareData, lastMissValues, matchCompareCountArr, matchCompareMaxArr);
            lastMissValues = missValues;

            rowData[column] = new String[size];
            rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
            rowData[column][1] = String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum());
            rowData[column][2] = String.valueOf(matchBean.getNum());
            rowData[column][3] = matchBean.getMatchStatus();

            System.arraycopy(missValues, 0, rowData[column], 4, missValues.length);
            column++;
        }
        //增加统计数据
        addStatisticsData(column, size, rowData, matchCompareCountArr, matchCompareMaxArr, 2, 5);
        column = column + 3;
        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.COMPARE_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 21, 22, 23});
        return setPanelScroll(table);
    }
}
