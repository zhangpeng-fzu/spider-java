package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MissValueDataBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

public class MatchComparePanelFactory extends PaneFactory {
    private static final MatchComparePanelFactory matchNumPanelFactory = new MatchComparePanelFactory();

    public static MatchComparePanelFactory getInstance() {
        return matchNumPanelFactory;
    }


    @Override
    public String[] getColumns(int index, String[] columnNames, int offset) {
        return Constants.INIT_COMPARE_DATA[index % Constants.INIT_COMPARE_DATA.length];
    }

    @Override
    public void fillTodayData(String[] tableRow, String[] columnNames, String[] curCompareData, int step, int offset) throws ParseException {
        tableRow[0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(DateUtil.getDateFormat().format(new Date())));
        for (int i = 1; i < columnNames.length; i++) {
            if (i >= offset && i % step == 0) {
                tableRow[i] = curCompareData[(i - offset) / step];
            } else {
                tableRow[i] = "";
            }
        }
    }


    @Override
    public void fillTableData(String[] tableRow, String[] missValues, MatchBean matchBean) throws ParseException {
        tableRow[0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
        tableRow[1] = String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum());
        tableRow[2] = String.valueOf(matchBean.getNum());
        tableRow[3] = matchBean.getMatchStatus();

        System.arraycopy(missValues, 0, tableRow, 4, missValues.length);
    }

    @Override
    public String[] calcMissValue(MatchBean matchBean, String[] curCompareData, String[] lastMissValues, int[] matchCompareCountArr, int[] matchCompareMaxArr, int[] matchCompareMax300Arr) {

        String[] missValues = new String[lastMissValues.length];
        System.arraycopy(lastMissValues, 0, missValues, 0, lastMissValues.length);

        String matchStatus = matchBean.getMatchStatus();
        for (int i = 0; i < curCompareData.length; i++) {
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
                missValues[2 * i + 1] = String.valueOf(Integer.parseInt(missValues[i * 2 + 1].equals("中") ? "0" : missValues[i * 2 + 1]) + 1);
                if (matchCompareMaxArr != null) {
                    matchCompareMaxArr[i] = Math.max(matchCompareMaxArr[i], Integer.parseInt(missValues[2 * i + 1]));
                }
                if (matchCompareMax300Arr != null) {
                    matchCompareMax300Arr[i] = Math.max(matchCompareMax300Arr[i], Integer.parseInt(missValues[2 * i + 1]));
                }
            }
        }
        return missValues;
    }

    /**
     * 按照日期获取进球数据
     *
     * @param date 选择日期
     * @return
     */
    public JScrollPane showMatchPaneByDate(Date date) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS_DATE;
        int size = columnNames.length;
        Map<String, MatchBean> matchBeans = LiveDataRepository.getMatchMap(date);
        String[][] rowData = new String[Math.max(matchBeans.size(), 10)][size];
        int column = 0;
        int step = 2;
        int offset = 4;
        for (String matchNum : matchBeans.keySet().stream().sorted(Comparator.comparing(String::trim)).collect(Collectors.toCollection(LinkedHashSet::new))) {
            //只显示未完成的场次
            if (this.skipMatchNum(date, matchNum)) {
                continue;
            }
            rowData[column][0] = matchNum;

            MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.COMPARE_TABLE, step, offset);
            String[][] missValueData = missValueDataBean.getMissValueData();

            //合并今天和昨天的数据
            String[] todayMiss = missValueData[missValueData.length - 5];
            String[] yesterdayMiss = missValueData[missValueData.length - 6];
            for (int i = 0; i < todayMiss.length; i++) {
                if (i % step != 0) {
                    todayMiss[i] = yesterdayMiss[i];
                }
            }

            System.arraycopy(todayMiss, offset, rowData[column], 1, todayMiss.length - offset);
            column++;
        }

        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setName(Constants.COMPARE_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, getSortColumn(size));
        return new JScrollPane(table);
    }

    JScrollPane showMatchPaneByNum(String matchNum) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS;
        MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.COMPARE_TABLE, 2, 4);
        String[][] tableData = missValueDataBean.getMissValueData();
        int size = columnNames.length;
        JTable table = new JTable(tableData, columnNames);
        table.setName(Constants.COMPARE_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        this.setTableHeader(table).setTableCell(table).setTableSorter(table, getSortColumn(size));
        return setPanelScroll(table);
    }
}
