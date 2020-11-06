package com.peng.frame.panel;

import com.peng.constant.Constants;
import com.peng.frame.MCellRenderer;
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
import java.util.Comparator;
import java.util.Date;

public class PaneFactory {


    static boolean isUnPlaying(String status) {
        return !Constants.PLAYING.equals(status);
    }

    static boolean isUnFinished(String status) {
        return !Constants.FINISHED.equals(status);
    }

    static boolean isCancelled(String status) {
        return Constants.CANCELLED.equals(status);
    }


    static void addStatisticsData(int column, int size, String[][] rowData, int[] countArr, int[] maxArr, int[] max300Arr, int step, int offset) {
        int total = column - 1;

        rowData[column] = new String[size];
        rowData[column][0] = Constants.TOTAL_MISS;
        for (int i = 0; i < countArr.length; i++) {
            rowData[column][i * step + offset] = handleTableData(countArr[i]);
        }
        column++;
        rowData[column] = new String[size];
        if (max300Arr == null) {
            rowData[column][0] = Constants.AVG_MISS;
            for (int i = 0; i < countArr.length; i++) {
                if (countArr[i] == 0) {
                    rowData[column][i * step + offset] = handleTableData(total);
                } else {
                    rowData[column][i * step + offset] = handleTableData(total / countArr[i]);
                }
            }
        } else {
            rowData[column][0] = Constants.MAX_300_MISS;
            for (int i = 0; i < max300Arr.length; i++) {
                rowData[column][i * step + offset] = handleTableData(max300Arr[i]);
            }
        }

        column++;
        rowData[column] = new String[size];
        rowData[column][0] = Constants.MAX_MISS;
        for (int i = 0; i < maxArr.length; i++) {
            rowData[column][i * step + offset] = handleTableData(maxArr[i]);
        }

        if (max300Arr != null) {
            //记录最大遗漏值，遗漏值达到最大遗漏值的80%，底色改为黄色
            Constants.MAX_MISS_VALUE_ARR = rowData[column];
        }
    }

    static String handleTableData(int value) {
        return value + " ";
    }

    Integer[] getSortColumn(int size) {
        Integer[] sortColumn = new Integer[size];
        for (int i = 0; i < size; i++) {
            sortColumn[i] = i;
        }
        return sortColumn;
    }

    boolean skipMatchNum(Date date, String matchNum) {
        return !Constants.MATCH_STATUS_MAP.containsKey(matchNum) ||
                (DateUtil.isToday(date) && (isUnPlaying(Constants.MATCH_STATUS_MAP.get(matchNum)) || isCancelled(Constants.MATCH_STATUS_MAP.get(matchNum))))
                || (!DateUtil.isToday(date) && isCancelled(Constants.MATCH_STATUS_MAP.get(matchNum)));
    }

    PaneFactory setTableHeader(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(25);

        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(110);

        if (table.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF) {
            for (int i = 1; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(50);
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


    JScrollPane setPanelScroll(JTable table) {
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
