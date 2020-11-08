package com.peng.frame;

import com.peng.frame.panel.*;
import com.peng.service.SyncTodayData;
import com.peng.util.DateUtil;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;

@Log
public class LiveScoreFrame extends JFrame {

    private static final long serialVersionUID = 3218784607640603309L;
    private static boolean isToday = true;
    private static JTabbedPane jTabbedPane;

    public LiveScoreFrame() throws ParseException {
        super();
        setTitle("稳操胜券竞彩分析软件");

        setBounds(300, 200, 1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel controlPanel = new JPanel();
        JLabel dateLabel = new JLabel("日期");
        JTextField selectedDateTxt = new JTextField(10);
        selectedDateTxt.setText(DateUtil.getDateFormat().format(new Date()));

        // 定义日历控件面板类
        CalendarPanel calendarPanel = new CalendarPanel(selectedDateTxt, "yyyy-MM-dd");
        getContentPane().add(calendarPanel);


        calendarPanel.initCalendarPanel();
        controlPanel.add(dateLabel);
        controlPanel.add(selectedDateTxt);

        JButton btn = new JButton("读取数据");

        controlPanel.add(btn);

        // 将滚动面板添加到边界布局的中间
        getContentPane().add(controlPanel, BorderLayout.NORTH);

        jTabbedPane = new JTabbedPane();
        Date selectedDate = DateUtil.getDateFormat().parse(selectedDateTxt.getText());
        jTabbedPane.add("当天赛事", MatchDataPanelFactory.getInstance().showMatchDataPane(selectedDate));
        jTabbedPane.add("串关分析", MatchCascadePanelFactory.getInstance().showMatchCascadePaneByDate(selectedDate));
        jTabbedPane.add("进球分析", MatchNumPanelFactory.getInstance().showMatchPaneByDate(selectedDate));
        jTabbedPane.add("进球对比", MatchComparePanelFactory.getInstance().showMatchPaneByDate(selectedDate));
        jTabbedPane.add("半全场分析", MatchHalfPanelFactory.getInstance().showMatchPaneByDate(selectedDate));

        jTabbedPane.setSelectedIndex(0);
        getContentPane().add(jTabbedPane, BorderLayout.CENTER);

        btn.addActionListener(e -> {
            isToday = selectedDateTxt.getText().equals(DateUtil.getDateFormat().format(new Date()));
            try {
                Date date = DateUtil.getDateFormat().parse(selectedDateTxt.getText());
                jTabbedPane.setComponentAt(0, MatchDataPanelFactory.getInstance().showMatchDataPane(date));
                jTabbedPane.setComponentAt(1, MatchCascadePanelFactory.getInstance().showMatchCascadePaneByDate(date));
                jTabbedPane.setComponentAt(2, MatchNumPanelFactory.getInstance().showMatchPaneByDate(date));
                jTabbedPane.setComponentAt(3, MatchComparePanelFactory.getInstance().showMatchPaneByDate(date));
                jTabbedPane.setComponentAt(4, MatchHalfPanelFactory.getInstance().showMatchPaneByDate(date));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });
    }


    public static void syncMatchData(boolean isFirst) {
        if (isFirst) {
            try {
                SyncTodayData.getMatchData();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            jTabbedPane.setComponentAt(0, MatchDataPanelFactory.getInstance().showMatchDataPane(new Date()));
        }

        new Thread(() -> {
            //当前不是今天，停止同步
            while (isToday) {
                System.out.println(String.format("正在同步%s的数据", DateUtil.getDateFormat(1).format(new Date())));

                try {
                    SyncTodayData.getMatchData();
                    jTabbedPane.setComponentAt(0, MatchDataPanelFactory.getInstance().showMatchDataPane(new Date()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }
}

