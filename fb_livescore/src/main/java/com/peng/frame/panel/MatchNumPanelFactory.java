package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchNumBean;
import com.peng.bean.MissValueDataBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchNumRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.List;
import java.util.*;

public class MatchNumPanelFactory extends PaneFactory {
    private static MatchNumPanelFactory matchNumPanelFactory;

    static {
        matchNumPanelFactory = new MatchNumPanelFactory();
    }

    public static MatchNumPanelFactory getInstance() {
        return matchNumPanelFactory;
    }

    static boolean skipMatchNum(Date date, String matchNum) {
        return !Constants.MATCH_STATUS_MAP.containsKey(matchNum) ||
                (DateUtil.isToday(date) && (!isPlaying(Constants.MATCH_STATUS_MAP.get(matchNum)) || isCancelled(Constants.MATCH_STATUS_MAP.get(matchNum))))
                || (!DateUtil.isToday(date) && (isUnFinished(Constants.MATCH_STATUS_MAP.get(matchNum)) || isCancelled(Constants.MATCH_STATUS_MAP.get(matchNum))));
    }

    public MissValueDataBean getMissValueData(String matchNum, boolean statistics) throws ParseException {
        String[] columnNames = Constants.MATCH_NUM_COLUMNS_DATE;
        int size = columnNames.length;
        int row = 0;

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
                if (!statistics) {
                    row++;
                }
                continue;
            }

            //当天未完成的场次 显示空行
            if (matchBean.getLiveDate().equals(today)) {
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
        return MissValueDataBean.builder().missValueData(tableData).row(row).build();
    }

    private String[] calcMissValue(MatchBean matchBean, String[] columns, String[] lastMissValues, int[] matchCountArr, int[] matchMaxArr, int[] matchMax300Arr) throws ParseException {

        String[] missValues = new String[lastMissValues.length];

        int matchNum = matchBean.getNum();

        for (int i = 0; i < columns.length; i++) {

            if (isHit(matchNum, i)) {
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
    public JScrollPane showMatchNumPaneByDate(Date date) {
        String[] columnNames = Constants.MATCH_NUM_COLUMNS;
        int size = columnNames.length;
        java.util.List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumData(date);
        String[][] rowData = new String[matchNumBeans.size() + 1][size];
        int column = 0;
        int[] matchNumCountArr = new int[size - 1];
        for (MatchNumBean matchNumBean : matchNumBeans) {
            //只显示未完成的场次
            if (skipMatchNum(date, matchNumBean.getMatchNum())) {
                continue;
            }
            rowData[column][0] = matchNumBean.getMatchNum();

            for (int i = 0; i < Constants.MATCH_NUM_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchNumBean.class.getDeclaredField(Constants.MATCH_NUM_FIELD_ARR[i]);
                    field.setAccessible(true);
                    rowData[column][i + 1] = String.valueOf(field.get(matchNumBean));
                    if (isHit(String.valueOf(field.get(matchNumBean)))) {
                        matchNumCountArr[i]++;
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            column++;
        }
        rowData[column] = new String[size];
        rowData[column][0] = Constants.TOTAL_MISS;
        for (int i = 0; i < matchNumCountArr.length; i++) {
            rowData[column][i + 1] = handleTableData(matchNumCountArr[i]);
        }
        column = column + 1;
        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.NUM_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        return new JScrollPane(table);
    }

    /**
     * 进球数据详情页
     *
     * @param matchNum 比赛场次
     * @return
     */
    JScrollPane showMatchNumPaneByNum(String matchNum) {
        String[] columnNames = Constants.MATCH_NUM_COLUMNS_DATE;
        int size = columnNames.length;
        java.util.List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumDataByNum(matchNum);
        String[][] rowData = new String[matchNumBeans.size() + 3][size];
        int column = 0;

        List<MatchBean> matchBeans = LiveDataRepository.getMatchListByNum(matchNum);
        Map<String, MatchBean> matchStatusMapByNum = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum.put(matchBean.getLiveDate(), matchBean);
        }
        int[] matchNumCountArr = new int[size - 1];
        int[] matchNumMaxArr = new int[size - 1];

        for (MatchNumBean matchNumBean : matchNumBeans) {
            String date = DateUtil.getDateFormat().format(matchNumBean.getLiveDate());
            //不显示已取消或者不存在的场次
            if (!matchStatusMapByNum.containsKey(date) || isCancelled(matchStatusMapByNum.get(date).getStatus())) {
                continue;
            }
            //当天未完成的场次 显示空行
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    isUnFinished(matchStatusMapByNum.get(date).getStatus())) {
                rowData[column] = new String[size];
                rowData[column][0] = DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate());
                column++;
                continue;
            }
            String[] missValues = new String[size - 1];
            for (int i = 0; i < Constants.MATCH_NUM_FIELD_ARR.length; i++) {
                String filedName = Constants.MATCH_NUM_FIELD_ARR[i];
                try {
                    Field field = MatchNumBean.class.getDeclaredField(filedName);
                    field.setAccessible(true);
                    missValues[i] = String.valueOf(field.get(matchNumBean));
                    if (isHit(String.valueOf(field.get(matchNumBean)))) {
                        missValues[i] = matchStatusMapByNum.get(date).getHostNum() + ":" + matchStatusMapByNum.get(date).getGuestNum();
                        matchNumCountArr[i]++;
                    }
                    matchNumMaxArr[i] = Math.max(matchNumMaxArr[i], Integer.parseInt(String.valueOf(field.get(matchNumBean))));
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            rowData[column] = new String[size];
            rowData[column][0] = DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate());
            System.arraycopy(missValues, 0, rowData[column], 1, missValues.length);
            column++;
        }
        addStatisticsData(column, size, rowData, matchNumCountArr, matchNumMaxArr, null, 1, 1);
        column = column + 3;

        String[][] newRowData = new String[column][size];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setName(Constants.NUM_DETAIL_TABLE);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        return setPanelScroll(table);
    }
}
