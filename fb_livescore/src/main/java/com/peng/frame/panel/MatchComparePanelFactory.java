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
    public String[] getColumns(int index, String[] columnNames, int offset, MatchBean matchBean, String[][] tableData, int row) {
        String[] compareData = Constants.INIT_COMPARE_DATA[index % Constants.INIT_COMPARE_DATA.length];
        if (index == 0 || row == 0) {
            return compareData;
        }

        String[] lastMissValues;
        try {
            lastMissValues = tableData[row - 1];
        } catch (Exception e) {
            return compareData;
        }

        int pos = 22;
        String lastValue = lastMissValues[pos * 2];
        String lastMissValue = lastMissValues[pos * 2 + 1];
        //确定23路，中反转，不中继续买，共三次
        if (lastMissValue.equals("中") || Integer.parseInt(lastMissValue) % 3 == 0) {
            compareData[pos] = lastValue.equals("单") ? "双" : "单";
        } else {
            compareData[pos] = lastValue;
        }

        pos = 23;
        lastValue = lastMissValues[pos * 2];
        lastMissValue = lastMissValues[pos * 2 + 1];

        //确定24路，上期中就反向，否则一直买
        if (lastMissValue.equals("中")) {
            compareData[pos] = lastValue.equals("单") ? "双" : "单";
        } else {
            compareData[pos] = lastValue;
        }

        pos = 24;
        lastValue = lastMissValues[pos * 2];
        lastMissValue = lastMissValues[pos * 2 + 1];

        //确定25路，上期中就买，否则反向
        if (lastMissValue.equals("中")) {
            compareData[pos] = lastValue;
        } else {
            compareData[pos] = lastValue.equals("单") ? "双" : "单";
        }

        pos = 25;

        //确定26路，今天买昨天开的结果，开爆买前天的
        compareData[pos] = matchBean.getMatchStatus();

        //爆买前天的
        if (matchBean.getMatchStatus().equals("爆")) {
            if (row >= 2) {
                compareData[pos] = tableData[row - 2][pos * 2];
            }
        } else {
            compareData[pos] = lastMissValues[pos * 2];
        }

        return compareData;
    }

    @Override
    public void fillTodayData(String[] tableRow, String[] columnNames, String[] compareData, int step, int offset) throws ParseException {
        tableRow[0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(DateUtil.getDateFormat().format(new Date())));
        for (int i = 1; i < columnNames.length; i++) {
            if (i >= offset && i % step == 0) {
                tableRow[i] = compareData[(i - offset) / step];
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
    public String[] calcMissValue(MatchBean matchBean, String[] compareData, String[] lastMissValues, int[] matchCompareCountArr, int[] matchCompareMaxArr, int[] matchCompareMax300Arr) {

        String[] missValues = new String[lastMissValues.length];
        System.arraycopy(lastMissValues, 0, missValues, 0, lastMissValues.length);

        String matchStatus = matchBean.getMatchStatus();
        for (int i = 0; i < compareData.length; i++) {
            if (compareData[i].equals("null")) {
                missValues[i] = "";
                continue;
            }
            missValues[2 * i] = compareData[i];
            //对比相等
            if (matchStatus.equals(compareData[i])) {
                missValues[2 * i + 1] = "中";
                if (matchCompareCountArr != null) {
                    matchCompareCountArr[i]++;
                }
                continue;
            }
            //不相等，遗漏值加1
            int newMissValue = Integer.parseInt(missValues[i * 2 + 1].equals("中") ? "0" : missValues[i * 2 + 1]) + 1;
            missValues[2 * i + 1] = String.valueOf(newMissValue);
            if (matchCompareMaxArr != null) {
                matchCompareMaxArr[i] = Math.max(matchCompareMaxArr[i], newMissValue);
            }
            if (matchCompareMax300Arr != null) {
                matchCompareMax300Arr[i] = Math.max(matchCompareMax300Arr[i], newMissValue);
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
    public JScrollPane showMatchPaneByDate(Date date, JFrame jFrame, JTable table) throws ParseException {

        String[] columnNames = Constants.MATCH_COMPARE_OVERVIEW_COLUMNS;
        int size = columnNames.length;
        if (table == null) {
            Map<String, MatchBean> matchBeans = LiveDataRepository.getMatchMap(date);
            String[][] rowData = new String[matchBeans.size()][size];
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
            table = new JTable(newRowData, columnNames);
            table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            this.setTableCell(table).setTableClick(table).setTableSorter(table, getSortColumn(size));
        }
        table.setName(Constants.COMPARE_TABLE);
        this.setTableHeader(table, jFrame);
        return new JScrollPane(table);
    }

    JScrollPane showMatchPaneByNum(String matchNum, JFrame jFrame, JTable table) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_DETAIL_COLUMNS;
        int size = columnNames.length;
        boolean isFirst = false;
        if (table == null) {
            isFirst = true;
            MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.COMPARE_TABLE, 2, 4);
            String[][] tableData = missValueDataBean.getMissValueData();
            table = new JTable(tableData, columnNames);
            this.setTableCell(table).setTableSorter(table, getSortColumn(size));
        }

        table.setName(Constants.COMPARE_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        this.setTableHeader(table, jFrame);
        if (!isFirst) {
            JScrollPane scrollPane = ((JScrollPane) (jFrame.getContentPane().getComponent(0)));
            scrollPane.setViewportView(table);
            return scrollPane;
        } else {
            return scrollToBottom(table);
        }
    }
}
