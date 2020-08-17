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
    public JScrollPane showMatchComparePaneByDate(Date date) {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS;
        int size = columnNames.length;
        List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumData(date);
        String[][] rowData = new String[matchNumBeans.size()][size];
        int column = 0;
        for (int i = 0; i < matchNumBeans.size(); i++) {
            MatchNumBean matchNumBean = matchNumBeans.get(i);
            //只显示未完成的场次
            if (!Constants.MATCH_STATUS_MAP.containsKey(matchNumBean.getMatchNum()) ||
                    (DateUtil.isToday(date) && !isPlaying(Constants.MATCH_STATUS_MAP.get(matchNumBean.getMatchNum())))
                    || (!DateUtil.isToday(date) && isUnFinished(Constants.MATCH_STATUS_MAP.get(matchNumBean.getMatchNum())))) {
                continue;
            }
            rowData[column] = new String[size];
            rowData[column][0] = matchNumBean.getMatchNum();
            String[] initCompareData = Constants.INIT_COMPARE_DATA[i % 10];
            System.arraycopy(initCompareData, 0, rowData[column], 1, initCompareData.length);
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

    /**
     * 进球数据详情页
     *
     * @param matchNum 比赛场次
     * @return
     */
    JScrollPane showMatchComparePaneByNum(String matchNum, String[] compareData) throws ParseException {
        String[] columnNames = Constants.MATCH_COMPARE_COLUMNS_DATE;
        int size = columnNames.length;
        List<MatchBean> matchList = LiveDataRepository.getMatchListByNum(matchNum);
        String[][] rowData = new String[matchList.size()][size];
        int column = 0;
        String[] lastMissValues = new String[compareData.length];
        Arrays.fill(lastMissValues, "0");
        for (MatchBean matchBean : matchList) {
            //当天未完成的场次 显示空行
            if (matchBean.getLiveDate().equals(DateUtil.getDateFormat().format(new Date())) &&
                    isUnFinished(matchBean.getStatus())) {
                rowData[column] = new String[size];
                rowData[column][0] = DateUtil.getDateFormat(1).format(DateUtil.getDateFormat().parse(matchBean.getLiveDate()));
                column++;
                continue;
            }
            String[] missValues = new String[compareData.length];
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
            for (int i = 0; i < compareData.length; i++) {
                if (compareData[i].equals("null")) {
                    missValues[i] = "";
                    continue;
                }
                missValues[i] = matchStatus.equals(compareData[i]) ? "中" : String.valueOf(Integer.parseInt(lastMissValues[i].equals("中") ? "0" : lastMissValues[i]) + 1);
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
        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        return setPanelScroll(table);
    }
}
