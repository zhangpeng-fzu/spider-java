package com.peng.frame;

import com.peng.bean.MatchBean;
import com.peng.bean.MatchCascadeBean;
import com.peng.bean.MatchNumBean;
import com.peng.repository.LiveDataRepository;
import com.peng.repository.MatchCascadeRepository;
import com.peng.repository.MatchNumRepository;
import com.peng.util.DateUtil;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

    private PaneFactory setTableHeader(JTable table) {
        table.setAutoCreateRowSorter(true);
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 30));
        table.setRowHeight(25);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setMinWidth(120);
        return this;
    }

    private PaneFactory setTableCell(JTable table) {
        DefaultTableCellRenderer tcr = new MCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);// 这句和上句作用一样
        table.setDefaultRenderer(Object.class, tcr);
        return this;
    }

    private PaneFactory setTableSorter(JTable table, Integer[] columns) {
        final TableRowSorter<TableModel> sorter = new TableRowSorter<>(
                table.getModel());

        for (Integer column : columns) {
            sorter.setComparator(column, (arg0, arg1) -> {
                try {
                    float a = Float.parseFloat(arg0.toString());
                    float b = Float.parseFloat(arg1.toString());
                    return (int) (a - b);
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

                innerFrame = new JFrame("详细数据");
                innerFrame.setBounds(400, 100, 640, 800);
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

        String[] columnNames = new String[]{"赛事编号", "比赛时间", "赛事", "状态", "主队", "客队", "胜赔率", "平赔率", "负赔率", "比分", "赛果"};// 定义表格列名数组
        java.util.List<MatchBean> matchBeanList = LiveDataRepository.getMatchData(date);

        String[][] rowData = new String[matchBeanList.size()][11];


        for (int i = 0; i < matchBeanList.size(); i++) {
            MatchBean matchBean = matchBeanList.get(i);
            //缓存比赛状态
            matchStatusMap.put(matchBean.getMatchNum().replaceAll("周[一|二|三|四|五|六|日]", ""), matchBean.getStatus());
            String result = matchBean.getResult();

            String status = matchBean.getStatus();
            switch (matchBean.getStatus()) {
                case "2":
                    status = "取消";
                    break;
                case "0":
                    status = "未";
                    result = "";
                    break;
                case "1":
                    status = "完";
                    break;
                default:
                    result = "";
                    break;
            }
            rowData[i] = new String[]{matchBean.getMatchNum(), matchBean.getLiveDate(), matchBean.getGroupName(), status, matchBean.getHostTeam(),
                    matchBean.getGuestTeam(), String.valueOf(matchBean.getOdds()[0]), String.valueOf(matchBean.getOdds()[1]), String.valueOf(matchBean.getOdds()[2]),
                    status.equals("完") ? String.format("%s:%s", matchBean.getHostNum(), matchBean.getGuestNum()) : "", result};
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
            if (DateUtil.getDateFormat().format(date).equals(DateUtil.getDateFormat().format(new Date()))
                    && (!matchStatusMap.containsKey(matchNums[0]) || !matchStatusMap.containsKey(matchNums[1]) ||
                    (!matchStatusMap.get(matchNums[0]).equals("0") && !matchStatusMap.get(matchNums[1]).equals("0")
//                            && !matchStatusMap.get(matchNums[0]).contains("分") && !matchStatusMap.get(matchNums[1]).contains("分")
                    ))
            ) {
                continue;
            }

            String[] odds = new String[9];
            if (matchCascadeBean.getOdds() != null && matchCascadeBean.getOdds().length() > 0) {
                odds = matchCascadeBean.getOdds().replace("[", "").replace("]", "").split(",");
            }
            int j = column * 9;
            rowData[j] = new String[]{matchCascadeBean.getMatchCascadeNum(), "胜胜", String.valueOf(matchCascadeBean.getSs()), odds[0]};
            rowData[j + 1] = new String[]{matchCascadeBean.getMatchCascadeNum(), "胜平", String.valueOf(matchCascadeBean.getSp()), odds[1]};
            rowData[j + 2] = new String[]{matchCascadeBean.getMatchCascadeNum(), "胜负", String.valueOf(matchCascadeBean.getSf()), odds[2]};
            rowData[j + 3] = new String[]{matchCascadeBean.getMatchCascadeNum(), "平胜", String.valueOf(matchCascadeBean.getPs()), odds[3]};
            rowData[j + 4] = new String[]{matchCascadeBean.getMatchCascadeNum(), "平平", String.valueOf(matchCascadeBean.getPp()), odds[4]};
            rowData[j + 5] = new String[]{matchCascadeBean.getMatchCascadeNum(), "平负", String.valueOf(matchCascadeBean.getPf()), odds[5]};
            rowData[j + 6] = new String[]{matchCascadeBean.getMatchCascadeNum(), "负胜", String.valueOf(matchCascadeBean.getFs()), odds[6]};
            rowData[j + 7] = new String[]{matchCascadeBean.getMatchCascadeNum(), "负平", String.valueOf(matchCascadeBean.getFp()), odds[7]};
            rowData[j + 8] = new String[]{matchCascadeBean.getMatchCascadeNum(), "负负", String.valueOf(matchCascadeBean.getFf()), odds[8]};
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
        String[] columnNames = new String[]{"赛事编号", "0球", "1球", "2球", "3球", "4球", "5球", "6球", "7球", "1球3球", "2球4球", "5球6球7球"
        };
        java.util.List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumData(date);
        String[][] rowData = new String[matchNumBeans.size()][12];
        int column = 0;
        for (MatchNumBean matchNumBean : matchNumBeans) {
            //只显示未完成的场次
            if (DateUtil.getDateFormat().format(date).equals(DateUtil.getDateFormat().format(new Date()))
                    && (!matchStatusMap.containsKey(matchNumBean.getMatchNum()) || !matchStatusMap.get(matchNumBean.getMatchNum()).equals("0"))) {
                continue;
            }
            rowData[column] = new String[]{matchNumBean.getMatchNum(), String.valueOf(matchNumBean.getZero()), String.valueOf(matchNumBean.getOne()),
                    String.valueOf(matchNumBean.getTwo()), String.valueOf(matchNumBean.getThree()), String.valueOf(matchNumBean.getFour()),
                    String.valueOf(matchNumBean.getFive()), String.valueOf(matchNumBean.getSix()), String.valueOf(matchNumBean.getSeven()),
                    String.valueOf(matchNumBean.getOne_three()), String.valueOf(matchNumBean.getTwo_four()),
                    String.valueOf(matchNumBean.getFive_())};
            column++;
        }
        String[][] newRowData = new String[column][12];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableClick(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        return new JScrollPane(table);
    }

    JScrollPane showMatchNumPaneByNum(String matchNum) {
        String[] columnNames = new String[]{"日期", "0球", "1球", "2球", "3球", "4球", "5球", "6球", "7球", "1球3球", "2球4球", "5球6球7球"
        };
        List<MatchNumBean> matchNumBeans = MatchNumRepository.getMatchNumDataByNum(matchNum);
        String[][] rowData = new String[matchNumBeans.size()][12];
        int column = 0;

        List<MatchBean> matchBeans = LiveDataRepository.getMatchListByNum(matchNum);
        Map<String, String> matchStatusMapByNum = new HashMap<>();
        for (MatchBean matchBean : matchBeans) {
            matchStatusMapByNum.put(matchBean.getLiveDate(), matchBean.getStatus());
        }
        for (MatchNumBean matchNumBean : matchNumBeans) {
            String date = DateUtil.getDateFormat().format(matchNumBean.getLiveDate());
            //不显示已取消或者不存在的场次
            if (!matchStatusMapByNum.containsKey(date)
                    || matchStatusMapByNum.get(date).equals("2")) {
                continue;
            }
            //当天未完成的场次 显示空行
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    !matchStatusMapByNum.get(date).equals("1")) {
                rowData[column] = new String[]{DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate()), "", "", "", "", "", "", "", "", "", "", ""};
            } else {
                rowData[column] = new String[]{DateUtil.getDateFormat(1).format(matchNumBean.getLiveDate()), String.valueOf(matchNumBean.getZero()), String.valueOf(matchNumBean.getOne()),
                        String.valueOf(matchNumBean.getTwo()), String.valueOf(matchNumBean.getThree()), String.valueOf(matchNumBean.getFour()),
                        String.valueOf(matchNumBean.getFive()), String.valueOf(matchNumBean.getSix()), String.valueOf(matchNumBean.getSeven()),
                        String.valueOf(matchNumBean.getOne_three()), String.valueOf(matchNumBean.getTwo_four()),
                        String.valueOf(matchNumBean.getFive_())};
            }
            column++;
        }

        String[][] newRowData = new String[column][12];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);
        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        return new JScrollPane(table);
    }

    JScrollPane showMatchCascadePaneByNum(String matchCascadeNum) {
        String[] columnNames = new String[]{"日期", "胜胜", "胜平", "胜负", "平胜", "平平", "平负", "负胜", "负平", "负负"};// 定义表格列名数组
        List<MatchCascadeBean> matchCascadeBeans = MatchCascadeRepository.getMatchCascadeDataByNum(matchCascadeNum);
        String[][] rowData = new String[matchCascadeBeans.size()][10];
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

        for (MatchCascadeBean matchCascadeBean : matchCascadeBeans) {
            //不显示已取消或者不存在的场次
            String date = DateUtil.getDateFormat().format(matchCascadeBean.getLiveDate());
            if (!matchStatusMapByNum1.containsKey(date) ||
                    !matchStatusMapByNum2.containsKey(date) ||
                    matchStatusMapByNum1.get(date).equals("2") ||
                    matchStatusMapByNum2.get(date).equals("2")) {
                continue;
            }
            if (date.equals(DateUtil.getDateFormat().format(new Date())) &&
                    (!matchStatusMapByNum1.get(date).equals("1") ||
                            !matchStatusMapByNum2.get(date).equals("1"))) {
                rowData[column] = new String[]{DateUtil.getDateFormat(1).format(matchCascadeBean.getLiveDate()), "", "", "", "", "", "", "", "", ""};
            } else {
                rowData[column] = new String[]{DateUtil.getDateFormat(1).format(matchCascadeBean.getLiveDate()),
                        String.valueOf(matchCascadeBean.getSs()), String.valueOf(matchCascadeBean.getSp()),
                        String.valueOf(matchCascadeBean.getSf()), String.valueOf(matchCascadeBean.getPs()),
                        String.valueOf(matchCascadeBean.getPp()), String.valueOf(matchCascadeBean.getPf()),
                        String.valueOf(matchCascadeBean.getFs()), String.valueOf(matchCascadeBean.getFp()),
                        String.valueOf(matchCascadeBean.getFf())};
            }
            column++;
        }


        String[][] newRowData = new String[column][10];
        System.arraycopy(rowData, 0, newRowData, 0, column);
        JTable table = new JTable(newRowData, columnNames);

        table.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.setTableHeader(table).setTableCell(table).setTableSorter(table, new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        return new JScrollPane(table);
    }
}
