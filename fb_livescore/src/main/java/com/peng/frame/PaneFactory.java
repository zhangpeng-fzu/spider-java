package com.peng.frame;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.bean.MatchNumBean;
import com.peng.constant.Constants;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.repository.MatchNumRepository;
import com.peng.util.DateUtil;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaneFactory {

    private static PaneFactory paneFactory;

    static {
        paneFactory = new PaneFactory();
    }

    private Map<String, String> matchStatusMap = new HashMap<>();
    private JFrame innerFrame = null;

    static PaneFactory getInstance() {
        return paneFactory;
    }

    private static boolean isPlaying(String status) {
        return Constants.PLAYING.equals(status);
    }

    private static boolean isFinished(String status) {
        return Constants.FINISHED.equals(status);
    }

    private static boolean isCancelled(String status) {
        return Constants.CANCELLED.equals(status);
    }

    private PaneFactory setTableHeader(JTable table) {
        table.setAutoCreateRowSorter(true);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 30));
        table.setRowHeight(25);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(110);

        DefaultTableCellHeaderRenderer hr = new DefaultTableCellHeaderRenderer();
        hr.setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setDefaultRenderer(hr);

        return this;
    }

    private PaneFactory setTableCell(JTable table) {
        DefaultTableCellRenderer tcr = new MCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, tcr);
        return this;
    }

    private PaneFactory setTableSorter(JTable table, Integer[] columns) {
        final TableRowSorter<TableModel> sorter = new TableRowSorter<>(
                table.getModel());

        for (Integer column : columns) {
            sorter.setComparator(column, (arg0, arg1) -> {
                try {
                    if (String.valueOf(arg0).contains(" ")) {
                        return 1;
                    }
                    if (String.valueOf(arg1).contains(" ")) {
                        return -1;
                    }

                    if (String.valueOf(arg0).equals("")) {
                        arg0 = "0";
                    }
                    if (String.valueOf(arg1).equals("")) {
                        arg1 = "0";
                    }
                    //有比分，设置成0
                    if (String.valueOf(arg0).contains(":")) {
                        arg0 = "0";
                    }
                    if (String.valueOf(arg1).contains(":")) {
                        arg1 = "0";
                    }
                    float a = Float.parseFloat(arg0.toString());
                    float b = Float.parseFloat(arg1.toString());
                    return a >= b ? 1 : -1;
                } catch (NumberFormatException e) {
                    return 0;
                }
            });
        }
        table.setRowSorter(sorter);
        return this;
    }

    private PaneFactory setTableClick(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String clickValue = String.valueOf(table.getValueAt(table.rowAtPoint(e.getPoint()), 0));

                innerFrame = new JFrame(clickValue + "详细数据");
                innerFrame.setBounds(400, 50, 550, 900);
                if (clickValue.contains("串")) {
                    innerFrame.getContentPane().add(showMatchCascadePaneByNum(clickValue));
                } else {
                    innerFrame.getContentPane().add(showMatchNumPaneByNum(clickValue));
                }
                innerFrame.setVisible(true);
            }

        });
        return this;
    }

    JScrollPane showMatchDataPane(Date date) {

        String[] columnNames = Constants.MATCH_COLUMNS;// 定义表格列名数组
        java.util.List<MatchBean> matchBeanList = LiveDataRepository.getMatchData(date);

        String[][] rowData = new String[matchBeanList.size()][11];


        for (int i = 0; i < matchBeanList.size(); i++) {
            MatchBean matchBean = matchBeanList.get(i);
            //缓存比赛状态
            matchStatusMap.put(matchBean.getMatchNum().replaceAll("周[一|二|三|四|五|六|日]", ""), matchBean.getStatus());
            String result = matchBean.getResult();

            String status = matchBean.getStatus();
            switch (matchBean.getStatus()) {
                case Constants.CANCELLED:
                    status = "取消";
                    break;
                case Constants.PLAYING:
                    status = "未";
                    result = "";
                    break;
                case Constants.FINISHED:
                    status = "完";
                    break;
                default:
                    result = "";
                    break;
            }
            rowData[i] = new String[]{matchBean.getMatchNum(), matchBean.getLiveDate(), matchBean.getGroupName(), status, matchBean.getHostTeam(),
                    matchBean.getGuestTeam(), String.valueOf(matchBean.getOdds()[0]), String.valueOf(matchBean.getOdds()[1]), String.valueOf(matchBean.getOdds()[2]),
                    status.equals("完") ? String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum()) : "", Constants.MATCH_RES_MAP.get(result)};
        }


        JTable table = new JTable(rowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table);
        return new JScrollPane(table);
    }

    JScrollPane showMatchCascadePane(Date date) {
        String[] columnNames = new String[]{"串关场次", "胜平负组合", "当前遗漏值", "赔率"};// 定义表格列名数组
        java.util.List<MatchCascadeBean> matchCascadeBeans = MatchCascadeRepository.getMatchCascadeData(date);
        String[][] rowData = new String[matchCascadeBeans.size() * 9][4];
        int column = 0;
        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            String[] matchNums = matchCascadeBean.getMatchCascadeNum().split("串");
            //只显示有比赛的场次
            if (!matchStatusMap.containsKey(matchNums[0]) || !matchStatusMap.containsKey(matchNums[1]) ||
                    (DateUtil.isToday(date) && (!isPlaying(matchStatusMap.get(matchNums[0])) || !isPlaying(matchStatusMap.get(matchNums[1]))))
                    || (!DateUtil.isToday(date) && (!isFinished(matchStatusMap.get(matchNums[0])) || !isFinished(matchStatusMap.get(matchNums[1])))
            )) {
                continue;
            }

            String[] odds = new String[9];
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

            int j = column * 9;
            String[] columns = Constants.MATCH_CASCADE_COLUMNS;
            for (int i = 0; i < Constants.MATCH_CASCADE_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchCascadeBean.class.getDeclaredField(Constants.MATCH_CASCADE_FIELD_ARR[i]);
                    field.setAccessible(true);
                    rowData[j + i] = new String[]{matchCascadeBean.getMatchCascadeNum(), columns[i], String.valueOf(field.get(matchCascadeBean)), odds[i]};

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            column++;
        }

        String[][] newRowData = new String[column * 9][4];
        if (column * 9 >= 0) {
            System.arraycopy(rowData, 0, newRowData, 0, column * 9);
        }


        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{2, 3});
        return new JScrollPane(table);
    }

    JScrollPane showMatchNumPaneByDate(Date date) {
        String[] columnNames = Constants.MATCH_NUM_COLUMNS;
        java.util.List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumData(date);
        String[][] rowData = new String[matchNumBeans.size()][16];
        int column = 0;
        for (MatchNumBean matchNumBean : matchNumBeans) {
            //只显示未完成的场次
            if (!matchStatusMap.containsKey(matchNumBean.getMatchNum()) ||
                    (DateUtil.isToday(date) && !isPlaying(matchStatusMap.get(matchNumBean.getMatchNum())))
                    || (!DateUtil.isToday(date) && !isFinished(matchStatusMap.get(matchNumBean.getMatchNum())))) {
                continue;
            }
            rowData[column] = new String[16];
            rowData[column][0] = matchNumBean.getMatchNum();
            for (int i = 0; i < Constants.MATCH_NUM_FIELD_ARR.length; i++) {
                try {
                    Field field = MatchNumBean.class.getDeclaredField(Constants.MATCH_NUM_FIELD_ARR[i]);
                    field.setAccessible(true);
                    rowData[column][i + 1] = String.valueOf(field.get(matchNumBean));

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            column++;
        }
        String[][] newRowData = new String[column][16];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});
        return new JScrollPane(table);
    }

    JScrollPane showMatchNumPaneByNum(String matchNum) {
        String[] columnNames = Constants.MATCH_NUM_COLUMNS_DATE;
        List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumDataByNum(matchNum);
        String[][] rowData = new String[matchNumBeans.size() + 3][16];
        int column = 0;

        List<MatchBean> matchBeans = LiveDataRepository.getMatchListByNum(matchNum);
        Map<String, MatchBean> matchStatusMapByNum = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum.put(matchBean.getLiveDate(), matchBean);
        }
        int[] matchNumCountArr = new int[15];
        int[] matchNumMaxArr = new int[15];

        for (MatchNumBean matchNumBean : matchNumBeans) {
            String date = DateUtil.getDateFormat().format(matchNumBean.getLiveDate());
            //不显示已取消或者不存在的场次
            if (!matchStatusMapByNum.containsKey(date) || isCancelled(matchStatusMapByNum.get(date).getStatus())) {
                continue;
            }
            //当天未完成的场次 显示空行
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    !isFinished(matchStatusMapByNum.get(date).getStatus())) {
                rowData[column] = new String[16];
                rowData[column][0] = DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate());
            } else {

                String[] missValues = new String[15];
                for (int i = 0; i < Constants.MATCH_NUM_FIELD_ARR.length; i++) {
                    String filedName = Constants.MATCH_NUM_FIELD_ARR[i];
                    try {
                        Field field = MatchNumBean.class.getDeclaredField(filedName);
                        field.setAccessible(true);
                        missValues[i] = String.valueOf(field.get(matchNumBean));
                        if (isPlaying(String.valueOf(field.get(matchNumBean)))) {
                            missValues[i] = matchStatusMapByNum.get(date).getHostNum() + ":" + matchStatusMapByNum.get(date).getGuestNum();
                            matchNumCountArr[i]++;
                        }
                        matchNumMaxArr[i] = Math.max(matchNumMaxArr[i], Integer.parseInt(String.valueOf(field.get(matchNumBean))));
                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                rowData[column] = new String[16];
                rowData[column][0] = DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate());
                System.arraycopy(missValues, 0, rowData[column], 1, missValues.length);
            }
            column++;
        }
        int total = column - 1;
        rowData[column] = new String[16];
        rowData[column][0] = Constants.TOTAL_MISS;
        for (int i = 0; i < matchNumCountArr.length; i++) {
            rowData[column][i + 1] = handleTableData(matchNumCountArr[i]);
        }
        column++;
        rowData[column] = new String[16];
        rowData[column][0] = Constants.AVG_MISS;
        for (int i = 0; i < matchNumCountArr.length; i++) {
            rowData[column][i + 1] = handleTableData(total / matchNumCountArr[i]);
        }
        column++;
        rowData[column] = new String[16];
        rowData[column][0] = Constants.MAX_MISS;
        for (int i = 0; i < matchNumMaxArr.length; i++) {
            rowData[column][i + 1] = handleTableData(matchNumMaxArr[i]);
        }

        String[][] newRowData = new String[++column][16];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15});

        JScrollPane sPane = new JScrollPane(table);
        JScrollBar sBar = sPane.getVerticalScrollBar();
        sBar.setValue(sBar.getMaximum());
        sPane.setVerticalScrollBar(sBar);

        int rowCount = table.getRowCount();
        table.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
        Rectangle rect = table.getCellRect(rowCount - 1, 0, true);
        table.scrollRectToVisible(rect);
        return sPane;
    }

    private String handleTableData(int value) {
        return value + " ";
    }

    JScrollPane showMatchCascadePaneByNum(String matchCascadeNum) {
        String[] columnNames = Constants.MATCH_CASCADE_COLUMNS_DATE;// 定义表格列名数组
        List<MatchCascadeBean> matchCascadeBeans = MatchCascadeRepository.getMatchCascadeDataByNum(matchCascadeNum);
        String[][] rowData = new String[matchCascadeBeans.size() + 3][10];
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
        int[] matchCascadeCountArr = new int[9];
        int[] matchCascadeMaxArr = new int[9];
        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            //不显示已取消或者不存在的场次
            String date = DateUtil.getDateFormat().format(matchCascadeBean.getLiveDate());
            if (!matchStatusMapByNum1.containsKey(date) || !matchStatusMapByNum2.containsKey(date) ||
                    isCancelled(matchStatusMapByNum1.get(date)) ||
                    isCancelled(matchStatusMapByNum2.get(date))) {
                continue;
            }
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    (!isFinished(matchStatusMapByNum1.get(date)) || !isFinished(matchStatusMapByNum2.get(date)))) {
                rowData[column] = new String[10];
                rowData[column][0] = DateUtil.getDateFormat(1).format(matchCascadeBean.getLiveDate());
            } else {
                rowData[column] = new String[10];
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
            }
            column++;
        }
        int total = column - 1;

        rowData[column] = new String[10];
        rowData[column][0] = Constants.TOTAL_MISS;
        for (int i = 0; i < matchCascadeCountArr.length; i++) {
            rowData[column][i + 1] = handleTableData(matchCascadeCountArr[i]);
        }
        column++;
        rowData[column] = new String[10];
        rowData[column][0] = Constants.AVG_MISS;
        for (int i = 0; i < matchCascadeCountArr.length; i++) {
            rowData[column][i + 1] = handleTableData(total / matchCascadeCountArr[i]);
        }
        column++;
        rowData[column] = new String[10];
        rowData[column][0] = Constants.MAX_MISS;
        for (int i = 0; i < matchCascadeMaxArr.length; i++) {
            rowData[column][i + 1] = handleTableData(matchCascadeMaxArr[i]);
        }

        String[][] newRowData = new String[++column][10];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);

        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        JScrollPane sPane = new JScrollPane(table);
        JScrollBar sBar = sPane.getVerticalScrollBar();
        sBar.setValue(sBar.getMaximum());
        sPane.setVerticalScrollBar(sBar);

        int rowCount = table.getRowCount();
        table.getSelectionModel().setSelectionInterval(rowCount - 1, rowCount - 1);
        Rectangle rect = table.getCellRect(rowCount - 1, 0, true);
        table.scrollRectToVisible(rect);

        return sPane;
    }
}
