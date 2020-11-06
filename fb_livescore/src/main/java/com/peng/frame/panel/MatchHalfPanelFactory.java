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

public class MatchHalfPanelFactory extends PaneFactory {
    private static final MatchHalfPanelFactory matchHalfPanelFactory = new MatchHalfPanelFactory();

    public static MatchHalfPanelFactory getInstance() {
        return matchHalfPanelFactory;
    }


    public MissValueDataBean getMissValueData(String matchNum, boolean statistics) throws ParseException {
        String[] columnNames = Constants.MATCH_HALF_COLUMNS_DATE;
        int size = columnNames.length;
        int row = 0;
        boolean hasToday = false;

        List<MatchBean> matchList = LiveDataRepository.getMatchListByNum(matchNum);

        //最大行数，中间可能有些行无需显示
        int maxRow = statistics ? matchList.size() + 3 : matchList.size();

        String[][] tableData = new String[maxRow][size];
        String[] lastMissValues = new String[size - 1];
        Arrays.fill(lastMissValues, "0");
        int[] matchCompareCountArr = new int[size - 1];
        int[] matchCompareMaxArr = new int[size - 1];
        int[] matchCompareMax300Arr = null;

        String today = DateUtil.getDateFormat().format(new Date());

        for (int index = 0; index < matchList.size(); index++) {
            MatchBean matchBean = matchList.get(index);

            //以往，未完成或者已取消的场次
            if (!matchBean.getLiveDate().equals(today) && isUnFinished(matchBean.getStatus())) {
                continue;
            }

            //当天未完成的场次 显示空行
            if (matchBean.getLiveDate().equals(today)) {
                hasToday = true;
                tableData[row] = new String[size];
                tableData[row][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
                for (int i = 1; i < columnNames.length; i++) {
                    tableData[row][i] = "";
                }
                row++;
                continue;
            }

            if (matchCompareMax300Arr == null && matchList.size() - index <= 300) {
                matchCompareMax300Arr = new int[size - 1];
            }

            String[] columns = new String[size - 1];
            System.arraycopy(columnNames, 1, columns, 0, columns.length);

            String[] missValues = calcMissValue(matchBean, columns, lastMissValues, matchCompareCountArr, matchCompareMaxArr, matchCompareMax300Arr);
            lastMissValues = missValues;

            tableData[row] = new String[size];
            tableData[row][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));

            System.arraycopy(missValues, 0, tableData[row], 1, missValues.length);
            row++;
        }
        if (statistics) {
            //增加统计数据
            addStatisticsData(row, size, tableData, matchCompareCountArr, matchCompareMaxArr, null, 1, 1);
            row = row + 3;
        }
        String[][] newTableData = new String[row][size];
        System.arraycopy(tableData, 0, newTableData, 0, row);
        return MissValueDataBean.builder().missValueData(newTableData).hasToday(hasToday).build();
    }

    private String[] calcMissValue(MatchBean matchBean, String[] columns, String[] lastMissValues, int[] matchCountArr, int[] matchMaxArr, int[] matchMax300Arr) throws ParseException {

        String[] missValues = new String[lastMissValues.length];
        String matchHalfResult = matchBean.getCNHalfResult();
        String matchResult = matchBean.getCNResult();

        String result = matchHalfResult + matchResult;

        for (int i = 0; i < columns.length; i++) {

            if (columns[i].contains(result)) {
                missValues[i] = "中";
                if (matchCountArr != null) {
                    matchCountArr[i]++;
                }
            } else {
                missValues[i] = String.valueOf(Integer.parseInt(lastMissValues[i].equals("中") ? "0" : lastMissValues[i]) + 1);
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


    /**
     * 按照日期获取进球数据
     *
     * @param date 选择日期
     * @return
     */
    public JScrollPane showMatchPaneByDate(Date date) throws ParseException {
        String[] columnNames = Constants.MATCH_HALF_COLUMNS;
        int size = columnNames.length;
        Map<String, MatchBean> matchBeans = LiveDataRepository.getMatchMap(date);
        String[][] rowData = new String[Math.max(matchBeans.size(), 10)][size];
        int column = 0;

        for (String matchNum : matchBeans.keySet().stream().sorted(Comparator.comparing(String::trim)).collect(Collectors.toCollection(LinkedHashSet::new))) {

            if (this.skipMatchNum(date, matchNum)) {
                continue;
            }

            rowData[column][0] = matchNum;
            String[][] missValueData = this.getMissValueData(matchNum, false).getMissValueData();
            //使用今天的预设数据和昨天的遗漏数据拼出概览数据
            String[] yesterdayMiss = missValueData[missValueData.length - 2];
            System.arraycopy(yesterdayMiss, 1, rowData[column], 1, yesterdayMiss.length - 1);
            column++;
        }

        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.HALF_TABLE);
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
        String[] columnNames = Constants.MATCH_HALF_COLUMNS;
        MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true);
        String[][] tableData = missValueDataBean.getMissValueData();

        int size = columnNames.length;

        JTable table = new JTable(tableData, columnNames);
        table.setName(Constants.HALF_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        this.setTableHeader(table).setTableCell(table).setTableSorter(table, getSortColumn(size));
        return new JScrollPane(table);
    }
}
