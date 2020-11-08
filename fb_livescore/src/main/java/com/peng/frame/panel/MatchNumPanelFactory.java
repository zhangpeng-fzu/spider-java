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

public class MatchNumPanelFactory extends PaneFactory {
    private static final MatchNumPanelFactory matchNumPanelFactory = new MatchNumPanelFactory();

    public static MatchNumPanelFactory getInstance() {
        return matchNumPanelFactory;
    }


    @Override
    public void fillTableData(String[] tableRow, String[] missValues, MatchBean matchBean) throws ParseException {
        tableRow[0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
        System.arraycopy(missValues, 0, tableRow, 1, missValues.length);
    }

    @Override
    protected void fillTodayData(String[] tableDatum, String[] columnNames, String[] curCompareData, int step, int offset) throws ParseException {
        tableDatum[0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(DateUtil.getDateFormat().format(new Date())));
        for (int i = 1; i < columnNames.length; i++) {
            tableDatum[i] = "";
        }
    }

    @Override
    public String[] getColumns(int index, String[] columnNames, int offset) {
        String[] columns = new String[columnNames.length - offset];
        System.arraycopy(columnNames, offset, columns, 0, columns.length);
        return columns;
    }

    @Override
    public String[] calcMissValue(MatchBean matchBean, String[] columns, String[] lastMissValues, int[] matchCountArr, int[] matchMaxArr, int[] matchMax300Arr) {

        String[] missValues = new String[lastMissValues.length];
        System.arraycopy(lastMissValues, 0, missValues, 0, lastMissValues.length);

        int matchNum = matchBean.getNum();

        for (int i = 0; i < columns.length; i++) {

            if (isHit(matchNum, i)) {
                missValues[i] = "0";
                if (matchCountArr != null) {
                    matchCountArr[i]++;
                }
            } else {
                missValues[i] = String.valueOf(Integer.parseInt(missValues[i].equals("中") ? "0" : missValues[i]) + 1);
                if (matchMaxArr != null) {
                    matchMaxArr[i] = Math.max(matchMaxArr[i], Integer.parseInt(missValues[i]));
                }
                if (matchMax300Arr != null) {
                    matchMax300Arr[i] = Math.max(matchMax300Arr[i], Integer.parseInt(missValues[i]));
                }
            }
        }
        return missValues;
    }

    private boolean isHit(int matchNum, int column) {
        switch (matchNum) {
            case 0:
                return column == 0 || column == 8;
            case 1:
                return column == 1 || column == 9;
            case 2:
                return column == 2 || column == 10;
            case 3:
                return column == 3 || column == 9;
            case 4:
                return column == 4 || column == 10;
            case 5:
                return column == 5 || column == 11;
            case 6:
                return column == 6 || column == 11;
            default:
                return column == 7 || column == 11;
        }
    }

    /**
     * 按照日期获取进球数据
     *
     * @param date 选择日期
     * @return
     */
    public JScrollPane showMatchPaneByDate(Date date) throws ParseException {
        String[] columnNames = Constants.MATCH_NUM_OVERVIEW_COLUMNS;
        int size = columnNames.length;
        Map<String, MatchBean> matchBeans = LiveDataRepository.getMatchMap(date);
        String[][] rowData = new String[Math.max(matchBeans.size(), 10)][size];
        int column = 0;

        for (String matchNum : matchBeans.keySet().stream().sorted(Comparator.comparing(String::trim)).collect(Collectors.toCollection(LinkedHashSet::new))) {

            if (this.skipMatchNum(date, matchNum)) {
                continue;
            }

            rowData[column][0] = matchNum;

            MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.NUM_TABLE, 1, 1);
            String[][] missValueData = missValueDataBean.getMissValueData();

            //使用今天的预设数据和昨天的遗漏数据拼出概览数据
            String[] yesterdayMiss = missValueData[missValueData.length - 6];
            System.arraycopy(yesterdayMiss, 1, rowData[column], 1, yesterdayMiss.length - 1);
            column++;
        }

        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.NUM_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, getSortColumn(size));
        return new JScrollPane(table);
    }

    /**
     * 进球数据详情页
     *
     * @param matchNum 比赛场次
     * @return
     */
    JScrollPane showMatchPaneByNum(String matchNum) throws ParseException {
        String[] columnNames = Constants.MATCH_NUM_DETAIL_COLUMNS;
        MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.NUM_TABLE, 1, 1);
        String[][] tableData = missValueDataBean.getMissValueData();
        int size = columnNames.length;

        JTable table = new JTable(tableData, columnNames);
        table.setName(Constants.NUM_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, getSortColumn(size));
        return scrollToBottom(table);
    }
}
