package com.peng.frame.panel;

import com.alibaba.fastjson.JSON;
import com.peng.bean.MatchBean;
import com.peng.bean.MissValueDataBean;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.frame.LiveScoreFrame;
import com.peng.repository.LiveDataRepository;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchCascadePanelFactory extends PaneFactory {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final LiveDataRepository liveDataRepository;

    public MatchCascadePanelFactory(LiveDataRepository liveDataRepository) {
        this.liveDataRepository = liveDataRepository;
    }

    /**
     * 按照日期获取串关数据
     *
     * @param frame
     * @param date  选择日期
     * @return
     */
    @Override
    public JScrollPane showMatchPaneByDate(JFrame frame, String date) throws ParseException {
        String[] columns = Constants.MATCH_CASCADE_COMMON;
        String[] columnNames = Constants.MATCH_CASCADE_OVERVIEW_COLUMNS;// 定义表格列名数组
        int size = columns.length;

        List<String> matchNums = liveDataRepository.findAllByLiveDate(date).stream().map(MatchBean::getMatchNum).collect(Collectors.toList());

        List<String> cascadeMatchNums = new ArrayList<>();

        for (int i = 0; i < matchNums.size(); i++) {
            for (int j = i + 1; j < matchNums.size(); j++) {
                cascadeMatchNums.add(String.format("%s串%s", matchNums.get(i), matchNums.get(j)));
            }
        }

        String[][] rowData = new String[cascadeMatchNums.size() * size][columnNames.length];
        int column = 0;

        //获取今天所有的比赛
        List<MatchBean> matchBeans = liveDataRepository.findAllByLiveDate(DATE_FORMAT.format(new Date()));
        Map<String,MatchBean> matchBeanMap = matchBeans.stream().collect(Collectors.toMap(MatchBean::getMatchNum, matchBean -> matchBean));

        for (String matchCascadeNum : cascadeMatchNums) {
            MissValueDataBean missValueDataBean = this.getMissValueData(date, matchCascadeNum, true, Constants.CASCADE_TABLE, 1, 1);
            String[][] missValueData = missValueDataBean.getMissValueData();
            String[] yesterdayMiss = missValueData[missValueData.length - 6];

            //获取赔率
            float[] odds = new float[9];
            String[] showOdds = new String[9];
            MatchBean matchBean = matchBeanMap.get(matchCascadeNum.split("串")[0]);
            MatchBean nextMatch = matchBeanMap.get(matchCascadeNum.split("串")[1]);
            if (matchBean != null && nextMatch != null){
                odds[0] = matchBean.getOddsS() * nextMatch.getOddsS();
                odds[1] = matchBean.getOddsS() * nextMatch.getOddsP();
                odds[2] = matchBean.getOddsS() * nextMatch.getOddsF();
                odds[3] = matchBean.getOddsP() * nextMatch.getOddsS();
                odds[4] = matchBean.getOddsP() * nextMatch.getOddsP();
                odds[5] = matchBean.getOddsP() * nextMatch.getOddsF();
                odds[6] = matchBean.getOddsF() * nextMatch.getOddsS();
                odds[7] = matchBean.getOddsF() * nextMatch.getOddsP();
                odds[8] = matchBean.getOddsF() * nextMatch.getOddsF();
            }

            for (int i = 0; i < odds.length; i++) {
                String odd = String.valueOf(odds[i]);
                if (odd.trim().length() > 5) {
                    odd = odd.trim().substring(0, 4);
                }
                showOdds[i] = odd;
            }
            //获取遗漏值
            int j = column * size;
            for (int i = 0; i < Constants.MATCH_CASCADE_COMMON.length; i++) {
                if (showOdds[i] == null) {
                    showOdds[i] = "";
                }
                rowData[j + i] = new String[]{matchCascadeNum, columns[i], yesterdayMiss[i + 1], showOdds[i].trim()};
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
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, getSortColumn(columnNames.length));
        return new JScrollPane(table);
    }

    /**
     * 获取串关详细数据
     *
     * @param matchNum 串关编号
     * @return
     */
    public JScrollPane showMatchPaneByNum(String matchNum) throws ParseException {
        String[] columnNames = Constants.MATCH_CASCADE_DETAIL_COLUMNS;
        MissValueDataBean missValueDataBean = this.getMissValueData(LiveScoreFrame.selectDate, matchNum, true, Constants.CASCADE_TABLE, 1, 1);
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

        //计算赔率
        missValues[lastMissValues.length - 1] = JSON.toJSONString(new float[9]);

        return missValues;
    }

    @Override
    protected void fillTableRow(String[] tableRow, String[] missValues, MatchBean matchBean) throws ParseException {
        tableRow[0] = matchBean.getLiveDate().replaceFirst("-", "年").replaceFirst("-", "月");
        System.arraycopy(missValues, 0, tableRow, 1, missValues.length);
    }

    @Override
    public String[] getColumns(int index, String[] columnNames, int offset, MatchBean matchBean, String[][] tableData, int row) {
        String[] columns = new String[columnNames.length - offset];
        System.arraycopy(columnNames, offset, columns, 0, columns.length);
        return columns;
    }
}
