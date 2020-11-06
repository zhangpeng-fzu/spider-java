package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MissValueDataBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MatchComparePanelFactory extends PaneFactory {
    private static final MatchComparePanelFactory matchNumPanelFactory = new MatchComparePanelFactory();

    public static MatchComparePanelFactory getInstance() {
        return matchNumPanelFactory;
    }


    private MissValueDataBean getMissValueData(String matchNum, boolean statistics) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS;
        int size = columnNames.length;
        int row = 0;
        int compareNum = (columnNames.length - 4) / 2;
        String today = DateUtil.getDateFormat().format(new Date());


        List<MatchBean> matchList = LiveDataRepository.getMatchListByNum(matchNum);
        boolean hasToday = matchList.stream().anyMatch(matchBean -> today.equals(matchBean.getLiveDate()));

        if (!hasToday) {
            matchList.add(MatchBean.builder().liveDate(today).build());
        }
        int maxRow = statistics ? matchList.size() + 3 : matchList.size();
        String[][] tableData = new String[maxRow][size];
        String[] lastMissValues = new String[size - 4];
        Arrays.fill(lastMissValues, "0");

        //统计数据
        int[] matchCompareCountArr = new int[compareNum];
        int[] matchCompareMaxArr = new int[compareNum];
        int[] matchCompareMax300Arr = null;

        String[] curCompareData;
        for (int index = 0; index < matchList.size(); index++) {
            MatchBean matchBean = matchList.get(index);
            curCompareData = Constants.INIT_COMPARE_DATA[index % Constants.INIT_COMPARE_DATA.length];
            //当天未完成的场次 显示空行
            if (matchBean.getLiveDate().equals(today)) {
                tableData[row] = new String[size];
                tableData[row][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(today));
                for (int i = 1; i < columnNames.length; i++) {
                    if (i >= 4 && i % 2 == 0) {
                        tableData[row][i] = curCompareData[(i - 4) / 2];
                    } else {
                        tableData[row][i] = "";
                    }
                }
                row++;
                continue;
            }
            //以往，未完成或者已取消的场次
            if (!matchBean.getLiveDate().equals(today) && isUnFinished(matchBean.getStatus())) {
                continue;
            }


            if (matchCompareMax300Arr == null && matchList.size() - index <= 300) {
                matchCompareMax300Arr = new int[compareNum];
            }

            String[] missValues = calcMissValue(matchBean, curCompareData, lastMissValues, matchCompareCountArr, matchCompareMaxArr, matchCompareMax300Arr);
            lastMissValues = missValues;

            tableData[row] = new String[size];
            tableData[row][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
            tableData[row][1] = String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum());
            tableData[row][2] = String.valueOf(matchBean.getNum());
            tableData[row][3] = matchBean.getMatchStatus();

            System.arraycopy(missValues, 0, tableData[row], 4, missValues.length);
            row++;
        }


        if (statistics) {
            //增加统计数据
            addStatisticsData(row, size, tableData, matchCompareCountArr, matchCompareMaxArr, matchCompareMax300Arr, 2, 5);
            row = row + 3;
        }
        String[][] newTableData = new String[row][size];
        System.arraycopy(tableData, 0, newTableData, 0, row);
        return MissValueDataBean.builder().missValueData(newTableData).build();
    }

    private String[] calcMissValue(MatchBean matchBean, String[] curCompareData, String[] lastMissValues, int[] matchCompareCountArr, int[] matchCompareMaxArr, int[] matchCompareMax300Arr) {

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
        for (String matchNum : matchBeans.keySet().stream().sorted(Comparator.comparing(String::trim)).collect(Collectors.toCollection(LinkedHashSet::new))) {
            //只显示未完成的场次
            if (this.skipMatchNum(date, matchNum)) {
                continue;
            }
            rowData[column][0] = matchNum;

            MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true);
            String[][] missValueData = missValueDataBean.getMissValueData();

            //合并今天和昨天的数据
            String[] todayMiss = missValueData[missValueData.length - 4];
            String[] yesterdayMiss = missValueData[missValueData.length - 5];
            for (int i = 0; i < todayMiss.length; i++) {
                if (i % 2 != 0) {
                    todayMiss[i] = yesterdayMiss[i];
                }
            }

            System.arraycopy(todayMiss, 4, rowData[column], 1, todayMiss.length - 4);
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
        MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true);
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
