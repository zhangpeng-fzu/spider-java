package com.peng.frame;

import com.peng.constant.Constants;
import com.peng.frame.panel.*;
import com.peng.service.MatchDataService;
import com.peng.util.SpringBeanUtils;
import lombok.extern.java.Log;
import org.springframework.core.task.TaskExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Log
public class LiveScoreFrame extends JFrame {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
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
        selectedDateTxt.setText(DATE_FORMAT.format(new Date()));

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
        TaskExecutor taskExecutor = SpringBeanUtils.getBean("taskExecutor");

        assert matchComparePanelFactory != null;
        assert matchDataPanelFactory != null;
        assert matchNumPanelFactory != null;
        assert matchHalfPanelFactory != null;
        assert matchCascadePanelFactory != null;

        jTabbedPane = new JTabbedPane();
        Date selectedDate = DATE_FORMAT.parse(selectedDateTxt.getText());
        jTabbedPane.add("当天赛事", new JScrollPane());
        jTabbedPane.add("串关分析", new JScrollPane());
        jTabbedPane.add("进球分析", new JScrollPane());
        jTabbedPane.add("进球对比", new JScrollPane());
        jTabbedPane.add("半全场分析", new JScrollPane());

        jTabbedPane.setSelectedIndex(0);
        getContentPane().add(jTabbedPane, BorderLayout.CENTER);
        this.syncMatchData();
        btn.addActionListener(e -> {
            PaneFactory[] paneFactories = new PaneFactory[]{matchDataPanelFactory, matchCascadePanelFactory, matchNumPanelFactory, matchComparePanelFactory, matchHalfPanelFactory};


            try {
                Date date = DATE_FORMAT.parse(selectedDateTxt.getText());
                int selectIndex = jTabbedPane.getSelectedIndex();
                jTabbedPane.setComponentAt(selectIndex, paneFactories[selectIndex].showMatchPaneByDate(date));

                paneFactories[selectIndex] = null;


                for (int i = 0; i < paneFactories.length; i++) {
                    PaneFactory paneFactory = paneFactories[i];
                    if (paneFactory == null) {
                        continue;
                    }
                    int finalI = i;
                    assert taskExecutor != null;
                    taskExecutor.execute(() -> {
                        try {
                            jTabbedPane.setComponentAt(finalI, paneFactory.showMatchPaneByDate(date));
                        } catch (ParseException parseException) {
                            parseException.printStackTrace();
                        }
                    });

                }


            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        });
        btn.doClick();
        this.addComponentListener(new ComponentAdapter() {//让窗口响应大小改变事件
            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    JViewport viewport = ((JScrollPane) (((JTabbedPane) (frame.getContentPane().getComponents()[2])).getComponentAt(3))).getViewport();
                    if (viewport.getComponents().length == 0) {
                        return;
                    }
                    JTable table = (JTable) viewport.getComponent(0);
                    if (Constants.COMPARE_TABLE.equals(table.getName())) {
                        Date date = DATE_FORMAT.parse(selectedDateTxt.getText());
                        jTabbedPane.setComponentAt(3, matchComparePanelFactory.showMatchDataPane(date, frame, table));
                    }

                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void syncMatchData() {
        MatchDataService matchDataService = SpringBeanUtils.getBean("matchDataService");
        MatchDataPanelFactory matchDataPanelFactory = SpringBeanUtils.getBean("matchDataPanelFactory");
        assert matchDataService != null;
        assert matchDataPanelFactory != null;


        new Thread(() -> {
            boolean refresh = false;

            //当前不是今天，停止同步
            while (true) {
                log.info(String.format("正在同步%s的数据", DATE_FORMAT.format(new Date())));

                try {
                    matchDataService.syncTodayMatch();
                    //第一次不刷新页面
                    if (refresh) {
                        jTabbedPane.setComponentAt(0, matchDataPanelFactory.showMatchPaneByDate(new Date()));
                    } else {
                        refresh = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(300000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }
}

