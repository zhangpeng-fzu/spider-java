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
            if (!Constants.MATCH_STATUS_MAP.containsKey(matchNumBean.getMatchNum()) ||
                    (DateUtil.isToday(date) && !isPlaying(Constants.MATCH_STATUS_MAP.get(matchNumBean.getMatchNum())))
                    || (!DateUtil.isToday(date) && isUnFinished(Constants.MATCH_STATUS_MAP.get(matchNumBean.getMatchNum())))) {
                continue;
            }
            rowData[column] = new String[size];
            rowData[column][0] = matchNumBean.getMatchNum();
            String[][] matchNumRowData = this.getRowData(matchNumBean.getMatchNum());

            String[] row = matchNumRowData[matchNumRowData.length - 4];
            String[] missRow = matchNumRowData[matchNumRowData.length - 5];
            for (int i = 4; i < row.length; i++) {
                if (i % 2 == 0) {
                    row[i] = missRow[i];
                }
            }
            if (row.length - 4 >= 0) {
                System.arraycopy(row, 4, rowData[column], 1, row.length - 4);
            }
            column++;
        }

        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.COMPARE_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table);
        return new JScrollPane(table);
    }

    private String[][] getRowData(String matchNum) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS_DATE;
        int size = columnNames.length;
        List<MatchBean> matchList = LiveDataRepository.getMatchListByNum(matchNum);
        String[][] rowData = new String[matchList.size() + 3][size];
        int column = 0;
        String[] lastMissValues = new String[20];
        Arrays.fill(lastMissValues, "0");
        for (int index = 0; index < matchList.size(); index++) {
            MatchBean matchBean = matchList.get(index);
            String[] curCompareData = Constants.INIT_COMPARE_DATA[index % 10];

            //当天未完成的场次 显示空行
            if (matchBean.getLiveDate().equals(DateUtil.getDateFormat().format(new Date()))) {
                rowData[column] = new String[size];
                rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
                for (int i = 0; i < curCompareData.length; i++) {
                    rowData[column][4 + i * 2] = curCompareData[i];
                }
                column++;
                continue;
            }
            String[] missValues = new String[20];
            String matchStatus;
            switch (matchBean.getNum()) {
                case 1:
                case 3:
                    matchStatus = "单";
                    break;
                case 2:
                case 4:
                    matchStatus = "双";
                    break;
                default:
                    matchStatus = "爆";
                    break;
            }
            for (int i = 0; i < Constants.INIT_COMPARE_DATA.length; i++) {
                if (curCompareData[i].equals("null")) {
                    missValues[i] = "";
                    continue;
                }
                missValues[2 * i] = curCompareData[i];
                if (matchStatus.equals(curCompareData[i])) {
                    missValues[2 * i + 1] = "中";
                } else {
                    missValues[2 * i + 1] = String.valueOf(Integer.parseInt(lastMissValues[i * 2 + 1].equals("中") ? "0" : lastMissValues[i * 2 + 1]) + 1);
                }
            }
            lastMissValues = missValues;

            rowData[column] = new String[size];
            rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
            rowData[column][1] = String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum());
            rowData[column][2] = String.valueOf(matchBean.getNum());
            rowData[column][3] = matchStatus;

            System.arraycopy(missValues, 0, rowData[column], 4, missValues.length);
            column++;
        }
        return rowData;
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
        for (int index = 0; index < matchList.size(); index++) {
            MatchBean matchBean = matchList.get(index);
            String[] curCompareData = Constants.INIT_COMPARE_DATA[index % 10];

            //当天未完成的场次 显示空行
            if (matchBean.getLiveDate().equals(DateUtil.getDateFormat().format(new Date()))) {
                rowData[column] = new String[size];
                rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
                for (int i = 0; i < curCompareData.length; i++) {
                    rowData[column][4 + i * 2] = curCompareData[i];
                }
                column++;
                continue;
            }
            String[] missValues = new String[20];
            String matchStatus;
            switch (matchBean.getNum()) {
                case 1:
                case 3:
                    matchStatus = "单";
                    break;
                case 2:
                case 4:
                    matchStatus = "双";
                    break;
                default:
                    matchStatus = "爆";
                    break;
            }
            for (int i = 0; i < Constants.INIT_COMPARE_DATA.length; i++) {
                if (curCompareData[i].equals("null")) {
                    missValues[i] = "";
                    continue;
                }
                missValues[2 * i] = curCompareData[i];
                if (matchStatus.equals(curCompareData[i])) {
                    missValues[2 * i + 1] = "中";
                    matchCompareCountArr[i]++;
                } else {
                    missValues[2 * i + 1] = String.valueOf(Integer.parseInt(lastMissValues[i * 2 + 1].equals("中") ? "0" : lastMissValues[i * 2 + 1]) + 1);
                    matchCompareMaxArr[i] = Math.max(matchCompareMaxArr[i], Integer.parseInt(missValues[2 * i + 1]));
                }
            }
            lastMissValues = missValues;

            rowData[column] = new String[size];
            rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
            rowData[column][1] = String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum());
            rowData[column][2] = String.valueOf(matchBean.getNum());
            rowData[column][3] = matchStatus;

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
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        return setPanelScroll(table);
    }
}
