package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.bean.MissValueDataBean;
import com.peng.constant.Constants;
import com.peng.constant.MatchStatus;
import com.peng.frame.MCellRenderer;
import com.peng.repository.LiveDataRepository;
import com.peng.util.DateUtil;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.List;
import java.util.*;

public abstract class PaneFactory {

    static boolean isUnPlaying(String status) {
        return !MatchStatus.PLAYING.equals(status);
    }

    static boolean isUnFinished(String status) {
        return !MatchStatus.FINISHED.equals(status);
    }

    static boolean isCancelled(String status) {
        return MatchStatus.CANCELLED.equals(status);
    }

    protected abstract String[] calcMissValue(MatchBean matchBean, String[] curCompareData, String[] lastMissValues, int[] matchCompareCountArr, int[] matchCompareMaxArr, int[] matchCompareMax300Arr) throws ParseException;

    protected abstract void fillTableData(String[] tableDatum, String[] missValues, MatchBean matchBean) throws ParseException;

    protected abstract void fillTodayData(String[] tableDatum, String[] columnNames, String[] curCompareData, int step, int offset) throws ParseException;

    public abstract String[] getColumns(int index, String[] columnNames, int offset, String[] lastMissValues);

    /**
     * 计算统计数据
     *
     * @param row       当前行数
     * @param tableData 表格数据
     * @param countArr  命中次数
     * @param maxArr    最大遗漏值
     * @param max300Arr 最近300最大遗漏值
     * @param step      步长
     * @param offset    偏移量
     */
    public void addStatisticsData(int row, String[][] tableData, int[] countArr, int[] maxArr, int[] max300Arr, int step, int offset) {
        int total = row - 1;
        int size = tableData[0].length;

        tableData[row] = new String[size];
        tableData[row][0] = Constants.TOTAL_MISS;
        for (int i = 0; i < countArr.length; i++) {
            tableData[row][i * step + offset] = String.valueOf(countArr[i]);
        }
        row++;
        tableData[row] = new String[size];

        tableData[row][0] = Constants.AVG_MISS;
        for (int i = 0; i < countArr.length; i++) {
            if (countArr[i] == 0) {
                tableData[row][i * step + offset] = String.valueOf(total);
            } else {
                tableData[row][i * step + offset] = String.valueOf(total / countArr[i]);
            }
        }
        row++;
        tableData[row] = new String[size];
        tableData[row][0] = Constants.MAX_300_MISS;
        if (max300Arr != null) {
            for (int i = 0; i < max300Arr.length; i++) {
                tableData[row][i * step + offset] = String.valueOf(max300Arr[i]);
            }
        }

        row++;
        tableData[row] = new String[size];
        tableData[row][0] = Constants.MAX_MISS;
        for (int i = 0; i < maxArr.length; i++) {
            tableData[row][i * step + offset] = String.valueOf(maxArr[i]);
        }
    }

    /**
     * 计算遗漏值
     *
     * @param matchNum   赛事编号
     * @param statistics 是否需要统计数据
     * @param type       统计表格类型
     * @param step       步长
     * @param offset     偏移量
     * @return 遗漏值数据
     * @throws ParseException e
     */
    public MissValueDataBean getMissValueData(String matchNum, boolean statistics, String type, int step, int offset) throws ParseException {
        String[] columnNames = Constants.TABLE_NAME_MAP.get(type)[1];
        String today = DateUtil.getDateFormat().format(new Date());

        int size = columnNames.length;
        int row = 0;
        int statisticsSize = (size - offset) / step;

        List<MatchBean> matchList = LiveDataRepository.getMatchListByNum(matchNum);
        if (matchList.stream().noneMatch(matchBean -> today.equals(matchBean.getLiveDate()))) {
            matchList.add(MatchBean.builder().liveDate(today).build());
        }
        //计算最大行数，统计数据占四行
        int maxRow = statistics ? matchList.size() + 4 : matchList.size();

        String[][] tableData = new String[maxRow][size];
        String[] lastMissValues = new String[size - offset];
        Arrays.fill(lastMissValues, "0");

        //命中次数
        int[] matchCountArr = new int[statisticsSize];
        //最大遗漏值
        int[] matchMaxArr = new int[statisticsSize];
        //最近300场最大遗漏值
        int[] matchMax300Arr = null;

        for (int index = 0; index < matchList.size(); index++) {
            MatchBean matchBean = matchList.get(index);
            String[] compareData = getColumns(index, columnNames, offset, lastMissValues);
            //当天的场次 显示空行
            if (matchBean.getLiveDate().equals(today)) {
                tableData[row] = new String[size];
                fillTodayData(tableData[row], columnNames, compareData, step, offset);
                row++;
                continue;
            }
            //以前未完成或者已取消的场次
            if (!matchBean.getLiveDate().equals(today) && isUnFinished(matchBean.getStatus())) {
                continue;
            }

            if (matchMax300Arr == null && matchList.size() - index <= 300) {
                matchMax300Arr = new int[statisticsSize];
            }
            //计算遗漏值
            String[] missValues = calcMissValue(matchBean, compareData, lastMissValues, matchCountArr, matchMaxArr, matchMax300Arr);

            //将算出来的遗漏值赋值给上一次的遗漏值
            lastMissValues = missValues;
            tableData[row] = new String[size];
            fillTableData(tableData[row], missValues, matchBean);
            row++;
        }

        if (statistics) {

            //增加统计数据
            addStatisticsData(row, tableData, matchCountArr, matchMaxArr, matchMax300Arr, step, offset + step - 1);
            row = row + 4;

            Map<String, String[]> maxMiss = Constants.MAX_MISS_VALUE_MAP.getOrDefault(type, new HashMap<>());
            maxMiss.put(matchNum, tableData[row - 1]);
            Constants.MAX_MISS_VALUE_MAP.put(type, maxMiss);

            Map<String, String[]> max300Miss = Constants.MAX_300_MISS_VALUE_MAP.getOrDefault(type, new HashMap<>());
            maxMiss.put(matchNum, tableData[row - 1]);
            Constants.MAX_300_MISS_VALUE_MAP.put(type, max300Miss);

        }
        String[][] newTableData = new String[row][size];
        System.arraycopy(tableData, 0, newTableData, 0, row);
        return MissValueDataBean.builder().missValueData(newTableData).build();
    }


    Integer[] getSortColumn(int size) {
        Integer[] sortColumn = new Integer[size];
        for (int i = 0; i < size; i++) {
            sortColumn[i] = i;
        }
        return sortColumn;
    }

    boolean skipMatchNum(Date date, String matchNum) {
        return !MatchStatus.MATCH_STATUS_MAP.containsKey(matchNum) ||
                (DateUtil.isToday(date) && (isUnPlaying(MatchStatus.MATCH_STATUS_MAP.get(matchNum)) || isCancelled(MatchStatus.MATCH_STATUS_MAP.get(matchNum))))
                || (!DateUtil.isToday(date) && isCancelled(MatchStatus.MATCH_STATUS_MAP.get(matchNum)));
    }

    PaneFactory setTableHeader(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(110);

        if (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
            for (int i = 1; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(35);
            }
        }

        DefaultTableCellHeaderRenderer hr = new DefaultTableCellHeaderRenderer();
        hr.setHorizontalAlignment(JLabel.CENTER);
        table.getTableHeader().setDefaultRenderer(hr);
        return this;
    }

    PaneFactory setTableCell(JTable table) {
        DefaultTableCellRenderer tcr = new MCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, tcr);
        return this;
    }

    void setTableSorter(JTable table, Integer[] columns) {
        final TableRowSorter<TableModel> sorter = new TableRowSorter<>(
                table.getModel());

        for (Integer column : columns) {
            sorter.setComparator(column, (Comparator<String>) (arg0, arg1) -> {
                try {
                    if (String.valueOf(arg0).contains(" ")) {
                        return 1;
                    }
                    if (String.valueOf(arg1).contains(" ")) {
                        return -1;
                    }

                    if (String.valueOf(arg0).equals("") || String.valueOf(arg0).equals("0.0") || arg0 == null || arg0.contains(":") || arg0.equals("中")) {
                        arg0 = "0";
                    }
                    if (String.valueOf(arg1).equals("") || String.valueOf(arg1).equals("0.0") || arg1 == null || arg1.contains(":") || arg1.equals("中")) {
                        arg1 = "0";
                    }

                    if (arg0.contains("年") || arg1.contains("年")) {
                        return arg0.compareTo(arg1);
                    }

                    Float a = Float.parseFloat(arg0);
                    Float b = Float.parseFloat(arg1);

                    return a.compareTo(b);
                } catch (NumberFormatException e) {
                    return 0;
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    return 0;
                }
            });
        }
        table.setRowSorter(sorter);
    }

    PaneFactory setTableClick(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    mouseSingleClicked(table, e);//执行单击事件
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }
            }
        });
        return this;
    }

    private void mouseSingleClicked(JTable table, MouseEvent e) throws ParseException {
        String clickValue = String.valueOf(table.getValueAt(table.rowAtPoint(e.getPoint()), 0));
        Constants.SELECT_MATCH_NUM = clickValue.trim();
        switch (table.getName()) {
            case Constants.NUM_TABLE:
                JFrame innerFrame = new JFrame(clickValue + "详细数据");
                innerFrame.setBounds(400, 50, 800, 900);
                innerFrame.getContentPane().add(MatchNumPanelFactory.getInstance().showMatchPaneByNum(clickValue));
                innerFrame.setVisible(true);

                break;
            case Constants.CASCADE_TABLE:
                innerFrame = new JFrame(clickValue + "详细数据");
                innerFrame.setBounds(400, 50, 650, 900);
                innerFrame.getContentPane().add(MatchCascadePanelFactory.getInstance().showMatchCascadePaneByNum(clickValue));
                innerFrame.setVisible(true);

                break;
            case Constants.COMPARE_TABLE:
                innerFrame = new JFrame(clickValue + "详细数据");
                innerFrame.setBounds(400, 50, 1000, 900);
                innerFrame.getContentPane().add(MatchComparePanelFactory.getInstance().showMatchPaneByNum(clickValue));

                innerFrame.setVisible(true);
                break;
            case Constants.HALF_TABLE:
                innerFrame = new JFrame(clickValue + "详细数据");
                innerFrame.setBounds(400, 50, 1000, 900);
                innerFrame.getContentPane().add(MatchHalfPanelFactory.getInstance().showMatchPaneByNum(clickValue));
                innerFrame.setVisible(true);
        }
    }


    JScrollPane scrollToBottom(JTable table) {
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
