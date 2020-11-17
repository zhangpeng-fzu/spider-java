package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MissValueDataBean;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchCascadePanelFactory extends PaneFactory {

    private final LiveDataRepository liveDataRepository;

    public MatchCascadePanelFactory(LiveDataRepository liveDataRepository) {
        this.liveDataRepository = liveDataRepository;
    }

    /**
     * 按照日期获取串关数据
     *
     * @param date 选择日期
     * @return
     */
    public JScrollPane showMatchDataPane(Date date) throws ParseException {
        String[] columns = Constants.MATCH_CASCADE_COMMON;
        String[] columnNames = Constants.MATCH_CASCADE_OVERVIEW_COLUMNS;// 定义表格列名数组
        int size = columns.length;

        List<String> matchNums = liveDataRepository.findAllByLiveDate(DateUtil.getDateFormat().format(date)).stream().map(matchBean -> matchBean.getMatchNum().substring(2)).collect(Collectors.toList());

        List<String> cascadeMatchNums = new ArrayList<>();

        for (int i = 0; i < matchNums.size(); i++) {
            for (int j = i + 1; j < matchNums.size(); j++) {
                cascadeMatchNums.add(String.format("%s串%s", matchNums.get(i), matchNums.get(j)));
            }
        }

        String[][] rowData = new String[cascadeMatchNums.size() * size][columnNames.length];
        int column = 0;

        for (String matchCascadeNum : cascadeMatchNums) {


            MissValueDataBean missValueDataBean = this.getMissValueData(matchCascadeNum, true, Constants.CASCADE_TABLE, 1, 1);
            String[][] missValueData = missValueDataBean.getMissValueData();

            String[] yesterdayMiss = missValueData[missValueData.length - 6];


            //获取赔率
            String[] odds = new String[size];

//            if (matchCascadeBean.getOdds() != null && matchCascadeBean.getOdds().length() > 0) {
//                odds = matchCascadeBean.getOdds().replace("[", "").replace("]", "").split(",");
//            }
//            for (int i = 0; i < odds.length; i++) {
//                String odd = odds[i];
//                if (odd != null && odd.trim().length() > 5) {
//                    odd = odd.trim().substring(0, 4);
//                }
//                odds[i] = odd;
//            }
            //获取遗漏值
            int j = column * size;
            for (int i = 0; i < Constants.MATCH_CASCADE_COMMON.length; i++) {
                if (odds[i] == null) {
                    odds[i] = "";
                }
                rowData[j + i] = new String[]{matchCascadeNum, columns[i], yesterdayMiss[i + 1], odds[i].trim()};
            }
            column++;
        }
        String[][] newRowData = new String[column * size][columnNames.length];
        if (column * size >= 0) {
            System.arraycopy(rowData, 0, newRowData, 0, column * size);
        }

        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.CASCADE_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{2, 3});
        return new JScrollPane(table);
    }

    /**
     * 获取串关详细数据
     *
     * @param matchNum 串关编号
     * @return
     */
    public JScrollPane showMatchCascadePaneByNum(String matchNum) throws ParseException {
        String[] columnNames = Constants.MATCH_CASCADE_DETAIL_COLUMNS;
        MissValueDataBean missValueDataBean = this.getMissValueData(matchNum, true, Constants.CASCADE_TABLE, 1, 1);
        String[][] tableData = missValueDataBean.getMissValueData();
        int size = columnNames.length;

        JTable table = new JTable(tableData, columnNames);
        table.setName(Constants.CASCADE_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, getSortColumn(size));
        return scrollToBottom(table);
    }

    @Override
    protected String[] calcMissValue(MatchBean matchBean, MatchBean nextMatch, String[] columns, String[] lastMissValues, int[] matchCountArr, int[] matchMaxArr, int[] matchMax300Arr) {

        if (matchBean == null || nextMatch == null || !matchBean.getStatus().equals(MatchStatus.FINISHED) ||
                !nextMatch.getStatus().equals(MatchStatus.FINISHED)) {
            return lastMissValues;
        }
        String[] missValues = new String[lastMissValues.length];
        System.arraycopy(lastMissValues, 0, missValues, 0, lastMissValues.length);

        String cascadeResult = matchBean.getCNResult() + nextMatch.getCNResult();

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(cascadeResult)) {
                missValues[i] = "0";
                if (matchCountArr != null) {
                    matchCountArr[i]++;
                }
            } else {
                missValues[i] = String.valueOf(Integer.parseInt(missValues[i]) + 1);
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
    protected void fillTableData(String[] tableRow, String[] missValues, MatchBean matchBean) throws ParseException {
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
    public String[] getColumns(int index, String[] columnNames, int offset, MatchBean matchBean, String[][] tableData, int row) {
        String[] columns = new String[columnNames.length - offset];
        System.arraycopy(columnNames, offset, columns, 0, columns.length);
        return columns;
    }
}
