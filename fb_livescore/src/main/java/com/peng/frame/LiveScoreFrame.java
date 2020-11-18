package com.peng.frame;

import com.peng.constant.Constants;
import com.peng.frame.panel.*;
import com.peng.service.SyncTodayData;
import com.peng.util.DateUtil;
import com.peng.util.SpringBeanUtils;
import lombok.extern.java.Log;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.ParseException;
import java.util.Date;

@Log
public class LiveScoreFrame extends JFrame {

    private static final long serialVersionUID = 3218784607640603309L;
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

        JFrame frame = this;

        MatchCascadePanelFactory matchCascadePanelFactory = SpringBeanUtils.getBean("matchCascadePanelFactory");
        MatchDataPanelFactory matchDataPanelFactory = SpringBeanUtils.getBean("matchDataPanelFactory");
        MatchNumPanelFactory matchNumPanelFactory = SpringBeanUtils.getBean("matchNumPanelFactory");
        MatchComparePanelFactory matchComparePanelFactory = SpringBeanUtils.getBean("matchComparePanelFactory");
        MatchHalfPanelFactory matchHalfPanelFactory = SpringBeanUtils.getBean("matchHalfPanelFactory");

        assert matchComparePanelFactory != null;
        assert matchDataPanelFactory != null;
        assert matchNumPanelFactory != null;
        assert matchHalfPanelFactory != null;
        assert matchCascadePanelFactory != null;

        jTabbedPane = new JTabbedPane();
        Date selectedDate = DateUtil.getDateFormat().parse(selectedDateTxt.getText());
        jTabbedPane.add("当天赛事", matchDataPanelFactory.showMatchDataPane(selectedDate));
        jTabbedPane.add("串关分析", matchCascadePanelFactory.showMatchDataPane(selectedDate));
        jTabbedPane.add("进球分析", matchNumPanelFactory.showMatchPaneByDate(selectedDate));
        jTabbedPane.add("进球对比", matchComparePanelFactory.showMatchPaneByDate(selectedDate, frame, null));
        jTabbedPane.add("半全场分析", matchHalfPanelFactory.showMatchPaneByDate(selectedDate));

        jTabbedPane.setSelectedIndex(0);
        getContentPane().add(jTabbedPane, BorderLayout.CENTER);
        this.syncMatchData(true);
        btn.addActionListener(e -> {
            //当天数据立即同步一次
            boolean isToday = selectedDateTxt.getText().equals(DateUtil.getDateFormat().format(new Date()));
            if (isToday) {
                this.syncMatchData(false);
            }
            try {
                Date date = DateUtil.getDateFormat().parse(selectedDateTxt.getText());
                jTabbedPane.setComponentAt(0, matchDataPanelFactory.showMatchDataPane(date));
                jTabbedPane.setComponentAt(1, matchCascadePanelFactory.showMatchDataPane(date));
                jTabbedPane.setComponentAt(2, matchNumPanelFactory.showMatchPaneByDate(date));
                jTabbedPane.setComponentAt(3, matchComparePanelFactory.showMatchPaneByDate(date, frame, null));
                jTabbedPane.setComponentAt(4, matchHalfPanelFactory.showMatchPaneByDate(date));
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });
        this.addComponentListener(new ComponentAdapter() {//让窗口响应大小改变事件
            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    JTable table = (JTable) ((JScrollPane) (((JTabbedPane) (frame.getContentPane().getComponents()[2])).getComponentAt(3))).getViewport().getComponent(0);
                    if (Constants.COMPARE_TABLE.equals(table.getName())) {
                        Date date = DateUtil.getDateFormat().parse(selectedDateTxt.getText());
                        jTabbedPane.setComponentAt(3, matchComparePanelFactory.showMatchPaneByDate(date, frame, table));
                    }

                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }


    public void syncMatchData(boolean isFirst) {
        SyncTodayData syncTodayData = SpringBeanUtils.getBean("syncTodayData");
        MatchDataPanelFactory matchDataPanelFactory = SpringBeanUtils.getBean("matchDataPanelFactory");
        assert syncTodayData != null;
        assert matchDataPanelFactory != null;

        if (isFirst) {
            try {
                syncTodayData.syncTodayMatch();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            jTabbedPane.setComponentAt(0, matchDataPanelFactory.showMatchDataPane(new Date()));
        }

        new Thread(() -> {
            //当前不是今天，停止同步
            while (true) {
                System.out.printf("正在同步%s的数据%n", DateUtil.getDateFormat(1).format(new Date()));

                try {
                    syncTodayData.syncTodayMatch();
                    jTabbedPane.setComponentAt(0, matchDataPanelFactory.showMatchDataPane(new Date()));
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

