package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MissValueDataBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchHalfPanelFactory extends PaneFactory {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DATE_FORMAT_CN = new SimpleDateFormat("yyyy年MM月dd日");
    private final LiveDataRepository liveDataRepository;

    public MatchHalfPanelFactory(LiveDataRepository liveDataRepository) {
        this.liveDataRepository = liveDataRepository;
    }

    @Override
    public String[] calcMissValue(MatchBean matchBean, MatchBean nextMatch, String[] columns, String[] lastMissValues, int[] matchCountArr, int[] matchMaxArr, int[] matchMax300Arr) throws ParseException {

        String[] missValues = new String[lastMissValues.length];
        System.arraycopy(lastMissValues, 0, missValues, 0, lastMissValues.length);
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

    @Override
    protected void fillTableData(String[] tableDatum, String[] missValues, MatchBean matchBean) throws ParseException {
        tableDatum[0] = DATE_FORMAT_CN.format(DATE_FORMAT.parse(matchBean.getLiveDate()));
        System.arraycopy(missValues, 0, tableDatum, 1, missValues.length);
    }

    @Override
    protected void fillTodayData(String[] tableDatum, String[] columnNames, String[] curCompareData, int step, int offset) throws ParseException {
        tableDatum[0] = DATE_FORMAT_CN.format(DATE_FORMAT.parse(DATE_FORMAT.format(new Date())));
        for (int i = 1; i < columnNames.length; i++) {
            tableDatum[i] = "";
        }
    }

    @Override
    public String[] getColumns(int index, String[] columnNames, int offset, MatchBean matchBean, String[][] tableData, int row) {
        String[] columns = new String[columnNames.length - offset];
        System.arraycopy(columnNames, offset, columns, 0, columns.length);
        return columns;
    }


    /**
     * 按照日期获取进球数据
     *
     * @param date 选择日期
     * @return
     */
    @Override
    public JScrollPane showMatchPaneByDate(Date date) throws ParseException {
        String[] columnNames = Constants.MATCH_HALF_OVERVIEW_COLUMNS;
        int size = columnNames.length;
        Map<String, MatchBean> matchBeans = liveDataRepository.findAllByLiveDate(DATE_FORMAT.format(date)).stream().collect(Collectors.toMap(matchBean -> matchBean.getMatchNum().substring(2), matchBean -> matchBean));
        String[][] rowData = new String[Math.max(matchBeans.size(), 10)][size];
        int column = 0;

        for (String matchNum : matchBeans.keySet().stream().sorted(Comparator.comparing(String::trim)).collect(Collectors.toCollection(LinkedHashSet::new))) {

            if (this.skipMatchNum(date, matchNum)) {
                continue;
            }

            rowData[column][0] = matchNum;
            MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.HALF_TABLE, 1, 1);

            String[][] missValueData = missValueDataBean.getMissValueData();
            //使用今天的预设数据和昨天的遗漏数据拼出概览数据
            String[] yesterdayMiss = missValueData[missValueData.length - 6];
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
        String[] columnNames = Constants.MATCH_HALF_DETAIL_COLUMNS;
        MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.HALF_TABLE, 1, 1);
        String[][] tableData = missValueDataBean.getMissValueData();

        int size = columnNames.length;

        JTable table = new JTable(tableData, columnNames);
        table.setName(Constants.HALF_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));


        this.setTableHeader(table).setTableCell(table).setTableSorter(table, getSortColumn(size));
        return scrollToBottom(table);
    }
}
