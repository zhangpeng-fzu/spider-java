package com.peng.frame;

import com.peng.service.ParseTodayData;
import com.peng.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.util.Date;

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
        JTextField txt1 = new JTextField(10);
        txt1.setText(DateUtil.getDateFormat().format(new Date()));

        // 定义日历控件面板类
        CalendarPanel calendarPanel = new CalendarPanel(txt1, "yyyy-MM-dd");
        getContentPane().add(calendarPanel);


        calendarPanel.initCalendarPanel();
        controlPanel.add(dateLabel);
        controlPanel.add(txt1);

        JButton btn = new JButton("读取数据");

        controlPanel.add(btn);

        // 将滚动面板添加到边界布局的中间
        getContentPane().add(controlPanel, BorderLayout.NORTH);

        jTabbedPane = new JTabbedPane();
        Date selectDate = DateUtil.getDateFormat().parse(txt1.getText());
        jTabbedPane.add("当天赛事", PaneFactory.getInstance().showMatchDataPane(selectDate));
        jTabbedPane.add("串关分析", PaneFactory.getInstance().showMatchCascadePane(selectDate));
        jTabbedPane.add("进球分析", PaneFactory.getInstance().showMatchNumPaneByDate(selectDate));

        jTabbedPane.setSelectedIndex(0);
        getContentPane().add(jTabbedPane, BorderLayout.CENTER);

        btn.addActionListener(e -> {
            isToday = txt1.getText().equals(DateUtil.getDateFormat().format(new Date()));

            try {
                jTabbedPane.setComponentAt(0, PaneFactory.getInstance().showMatchDataPane(DateUtil.getDateFormat().parse(txt1.getText())));
                jTabbedPane.setComponentAt(1, PaneFactory.getInstance().showMatchCascadePane(DateUtil.getDateFormat().parse(txt1.getText())));
                jTabbedPane.setComponentAt(2, PaneFactory.getInstance().showMatchNumPaneByDate(DateUtil.getDateFormat().parse(txt1.getText())));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });
    }


    public static void syncMatchData(boolean isFirst) {
        if (isFirst) {
            try {
                ParseTodayData.getMatchData();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            jTabbedPane.setComponentAt(0, PaneFactory.getInstance().showMatchDataPane(new Date()));
        }

        new Thread(() -> {
            //当前不是今天，停止同步
            while (isToday) {
                System.out.println(String.format("正在同步%s的数据", DateUtil.getDateFormat(1).format(new Date())));

                try {
                    ParseTodayData.getMatchData();
                    jTabbedPane.setComponentAt(0, PaneFactory.getInstance().showMatchDataPane(new Date()));
                } catch (ParseException e) {
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

