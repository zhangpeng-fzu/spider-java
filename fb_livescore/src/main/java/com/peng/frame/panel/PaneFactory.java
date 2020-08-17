package com.peng.frame.panel;

import com.peng.bean.MatchBean;
import com.peng.constant.Constants;
import com.peng.frame.MCellRenderer;
import com.peng.repository.LiveDataRepository;
import sun.swing.table.DefaultTableCellHeaderRenderer;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Date;

public class PaneFactory {
    private static PaneFactory paneFactory;

    static {
        paneFactory = new PaneFactory();
    }


    private JFrame innerFrame = null;

    public static PaneFactory getInstance() {
        return paneFactory;
    }

    static boolean isPlaying(String status) {
        return Constants.PLAYING.equals(status);
    }

    static boolean isUnFinished(String status) {
        return !Constants.FINISHED.equals(status);
    }

    static boolean isCancelled(String status) {
        return Constants.CANCELLED.equals(status);
    }

    static boolean isHit(String missValue) {
        return "0".equals(missValue);
    }

    static void addStatisticsData(int column, int size, String[][] rowData, int[] countArr, int[] maxArr) {
        int total = column - 1;

        rowData[column] = new String[size];
        rowData[column][0] = Constants.TOTAL_MISS;
        for (int i = 0; i < countArr.length; i++) {
            rowData[column][i + 1] = handleTableData(countArr[i]);
        }
        column++;
        rowData[column] = new String[size];
        rowData[column][0] = Constants.AVG_MISS;
        for (int i = 0; i < countArr.length; i++) {
            if (countArr[i] == 0) {
                rowData[column][i + 1] = handleTableData(total);
            } else {
                rowData[column][i + 1] = handleTableData(total / countArr[i]);
            }
        }
        column++;
        rowData[column] = new String[size];
        rowData[column][0] = Constants.MAX_MISS;
        for (int i = 0; i < maxArr.length; i++) {
            rowData[column][i + 1] = handleTableData(maxArr[i]);
        }
    }

    static String handleTableData(int value) {
        return value + " ";
    }

    PaneFactory setTableHeader(JTable table) {
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

    PaneFactory setTableCell(JTable table) {
        DefaultTableCellRenderer tcr = new MCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, tcr);
        return this;
    }

    PaneFactory setTableSorter(JTable table, Integer[] columns) {
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

                    if (String.valueOf(arg0).equals("") || String.valueOf(arg0).equals("0.0") || arg0 == null) {
                        arg0 = "0";
                    }
                    if (String.valueOf(arg1).equals("") || String.valueOf(arg1).equals("0.0") || arg1 == null) {
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

    PaneFactory setTableClick(JTable table) {
        table.addMouseListener(new MouseAdapter() {
            boolean flag = false;//用来判断是否已经执行双击事件
            int clickNum = 0;//用来判断是否该执行双击事件

            @Override
            public void mouseClicked(MouseEvent e) {
                this.flag = false;//每次点击鼠标初始化双击事件执行标志为false
                System.out.println("1=" + clickNum);
                if (clickNum == 1) {//当clickNum==1时执行双击事件
                    clickNum = 0;//初始化双击事件执行标志为0
                    flag = true;//双击事件已执行,事件标志为true
                    return;
                }

                //定义定时器
                java.util.Timer timer = new java.util.Timer();

                //定时器开始执行,延时0.2秒后确定是否执行单击事件
                timer.schedule(new java.util.TimerTask() {
                    private int n = 0;//记录定时器执行次数

                    @Override
                    public void run() {
                        if (flag) {//如果双击事件已经执行,那么直接取消单击执行
                            n = 0;
                            clickNum = 0;
                            this.cancel();
                            return;
                        }
                        if (n == 1) {//定时器等待0.2秒后,双击事件仍未发生,执行单击事件
                            mouseSingleClicked(table, e);//执行单击事件
                            flag = true;
                            clickNum = 0;
                            n = 0;
                            this.cancel();
                            return;
                        }
                        clickNum++;
                        n++;
                        System.out.println(clickNum);
                    }
                }, new java.util.Date(), 5000);
            }
        });
        return this;
    }

    private void mouseSingleClicked(JTable table, MouseEvent e) {
        System.out.println(e.getClickCount());
        String clickValue = String.valueOf(table.getValueAt(table.rowAtPoint(e.getPoint()), 0));

        switch (table.getName()) {
            case Constants.NUM_TABLE:
                innerFrame = new JFrame(clickValue + "详细数据");
                innerFrame.setBounds(400, 50, 650, 900);
                innerFrame.getContentPane().add(MatchNumPanelFactory.getInstance().showMatchNumPaneByNum(clickValue));
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
                innerFrame.setBounds(400, 50, 800, 900);
                String[] compareData = new String[15];
                for (int i = 1; i <= 15; i++) {
                    compareData[i - 1] = String.valueOf(table.getValueAt(table.rowAtPoint(e.getPoint()), i));
                }
                try {
                    innerFrame.getContentPane().add(MatchComparePanelFactory.getInstance().showMatchComparePaneByNum(clickValue, compareData));
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }
                innerFrame.setVisible(true);
        }
    }

    public JScrollPane showMatchDataPane(Date date) {

        String[] columnNames = Constants.MATCH_COLUMNS;// 定义表格列名数组
        java.util.List<MatchBean> matchBeanList = LiveDataRepository.getMatchData(date);

        String[][] rowData = new String[matchBeanList.size()][11];


        for (int i = 0; i < matchBeanList.size(); i++) {
            MatchBean matchBean = matchBeanList.get(i);
            //缓存比赛状态
            Constants.MATCH_STATUS_MAP.put(matchBean.getMatchNum().replaceAll("周[一|二|三|四|五|六|日]", ""), matchBean.getStatus());
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
