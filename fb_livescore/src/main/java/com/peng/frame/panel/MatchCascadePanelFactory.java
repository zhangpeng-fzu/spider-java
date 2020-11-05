package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class MatchCascadePanelFactory extends PaneFactory {
    private static MatchCascadePanelFactory matchCascadePanelFactory;

    static {
        matchCascadePanelFactory = new MatchCascadePanelFactory();
    }

    public static MatchCascadePanelFactory getInstance() {
        return matchCascadePanelFactory;
    }

    /**
     * 按照日期获取串关数据
     *
     * @param date 选择日期
     * @return
     */
    public JScrollPane showMatchCascadePaneByDate(Date date) {
        String[] columns = Constants.MATCH_CASCADE_COLUMNS;
        String[] columnNames = new String[]{"串关场次", "胜平负组合", "当前遗漏值", "赔率"};// 定义表格列名数组
        int size = columns.length;

        Set<String> matchNumSet = new TreeSet<>(Comparator.naturalOrder());
        matchNumSet.addAll(Constants.MATCH_STATUS_MAP.keySet());
        List<String> matchNumList = new ArrayList<>(matchNumSet);


        List<String> cascadeMatchNums = new ArrayList<>();
        for (int i = 0; i < matchNumList.size(); i++) {
            for (int j = i + 1; j < matchNumList.size(); j++) {
                cascadeMatchNums.add(String.format("%s串%s", matchNumList.get(i), matchNumList.get(j)));
            }
        }

        List<MatchCascadeBean> matchCascadeBeans = MatchCascadeRepository.findLatestCascadeData(date);
        Map<String, MatchCascadeBean> matchCascadeBeanMap = matchCascadeBeans.stream().collect(Collectors.toMap(MatchCascadeBean::getMatchCascadeNum, matchCascadeBean -> matchCascadeBean));

        String[][] rowData = new String[matchCascadeBeans.size() * size][columnNames.length];
        int column = 0;

        for (String matchCascadeNum : cascadeMatchNums) {
            String[] matchNums = matchCascadeNum.split("串");
            //只显示有比赛的场次
            if (!Constants.MATCH_STATUS_MAP.containsKey(matchNums[0]) || !Constants.MATCH_STATUS_MAP.containsKey(matchNums[1]) ||
                    (DateUtil.isToday(date) && (!isPlaying(Constants.MATCH_STATUS_MAP.get(matchNums[0])) || !isPlaying(Constants.MATCH_STATUS_MAP.get(matchNums[1]))))
                    || (!DateUtil.isToday(date) && (isUnFinished(Constants.MATCH_STATUS_MAP.get(matchNums[0])) || isUnFinished(Constants.MATCH_STATUS_MAP.get(matchNums[1])))
            )) {
                continue;
            }
            MatchCascadeBean matchCascadeBean = matchCascadeBeanMap.get(matchCascadeNum);
            if (matchCascadeBean == null) {
                continue;
            }
            //获取赔率
            String[] odds = new String[size];
            if (matchCascadeBean.getOdds() != null && matchCascadeBean.getOdds().length() > 0) {
                odds = matchCascadeBean.getOdds().replace("[", "").replace("]", "").split(",");
            }
            for (int i = 0; i < odds.length; i++) {
                String odd = odds[i];
                if (odd != null && odd.trim().length() > 5) {
                    odd = odd.trim().substring(0, 4);
                }
                odds[i] = odd;
            }
            //获取遗漏值
            int j = column * size;
            for (int i = 0; i < Constants.MATCH_CASCADE_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchCascadeBean.class.getDeclaredField(Constants.MATCH_CASCADE_FIELD_ARR[i]);
                    field.setAccessible(true);
                    if (odds[i] == null) {
                        odds[i] = "";
                    }
                    rowData[j + i] = new String[]{matchCascadeBean.getMatchCascadeNum(), columns[i], String.valueOf(field.get(matchCascadeBean)), odds[i].trim()};

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
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
     * @param matchCascadeNum 串关编号
     * @return
     */
    JScrollPane showMatchCascadePaneByNum(String matchCascadeNum) {
        String[] columnNames = Constants.MATCH_CASCADE_COLUMNS_DATE;
        int size = columnNames.length;
        java.util.List<MatchCascadeBean> matchCascadeBeans = MatchCascadeRepository.getMatchCascadeDataByNum(matchCascadeNum);
        String[][] rowData = new String[matchCascadeBeans.size() + 3][size];
        String[] matchNums = matchCascadeNum.split("串");

        //获取前面编号的状态
        List<MatchBean> matchBeans = LiveDataRepository.getMatchListByNum(matchNums[0]);
        Map<String, String> matchStatusMapByNum1 = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum1.put(matchBean.getLiveDate(), matchBean.getStatus());
        }

        //获取后面编号的状态
        matchBeans = LiveDataRepository.getMatchListByNum(matchNums[1]);
        Map<String, String> matchStatusMapByNum2 = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum2.put(matchBean.getLiveDate(), matchBean.getStatus());
        }
        int column = 0;
        int[] matchCascadeCountArr = new int[size - 1];
        int[] matchCascadeMaxArr = new int[size - 1];
        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            //不显示已取消或者不存在的场次
            String date = DateUtil.getDateFormat().format(matchCascadeBean.getLiveDate());
            if (!matchStatusMapByNum1.containsKey(date) || !matchStatusMapByNum2.containsKey(date) ||
                    isCancelled(matchStatusMapByNum1.get(date)) || isCancelled(matchStatusMapByNum2.get(date))) {
                continue;
            }
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    (isUnFinished(matchStatusMapByNum1.get(date)) || isUnFinished(matchStatusMapByNum2.get(date)))) {
                rowData[column] = new String[size];
                rowData[column][0] = DateUtil.getDateFormat(1).format(matchCascadeBean.getLiveDate());
                column++;
                continue;
            }
            rowData[column] = new String[size];
            rowData[column][0] = DateUtil.getDateFormat(1).format(matchCascadeBean.getLiveDate());

            for (int i = 0; i < Constants.MATCH_CASCADE_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchCascadeBean.class.getDeclaredField(Constants.MATCH_CASCADE_FIELD_ARR[i]);
                    field.setAccessible(true);
                    int value = Integer.parseInt(String.valueOf(field.get(matchCascadeBean)));
                    if (value == 0) {
                        matchCascadeCountArr[i]++;
                    }
                    matchCascadeMaxArr[i] = Math.max(matchCascadeMaxArr[i], value);
                    rowData[column][i + 1] = String.valueOf(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            column++;
        }
        //增加统计数据
        addStatisticsData(column, size, rowData, matchCascadeCountArr, matchCascadeMaxArr, null, 1, 1);
        column = column + 3;
        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, Constants.MATCH_CASCADE_COLUMNS_DATE);
        table.setName(Constants.CASCADE_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        return setPanelScroll(table);
    }

}
